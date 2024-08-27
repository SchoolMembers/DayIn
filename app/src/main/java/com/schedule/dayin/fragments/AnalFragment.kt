package com.schedule.dayin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AnalysisViewpagerBinding
import com.schedule.dayin.views.AnalCateAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class AnalFragment: Fragment() {

    private lateinit var date: LocalDate

    private lateinit var binding: AnalysisViewpagerBinding

    private lateinit var pieChart: PieChart

    //데이터
    private lateinit var appController: AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var cateRepository: CateRepository

    private lateinit var entries: ArrayList<PieEntry>

    //카테고리 분류 저장
    private fun saveCategory(category: Long, index: Long) {
        val cateString = category.toString()
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putLong(cateString, index)
        editor.apply()
    }

    // 0: 식비 1: 패션/미용 2: 음료/주류 3: 교통 4: 의료/건강 5: 주거 6: 교육 7: 여가 8: 생활 9: 기타
    private fun loadCategory(category: Long): Long {
        val cateString = category.toString()
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        return pref.getLong(cateString, -1L)
    }

    //값이 일치하는 모든 키 불러오기
    private fun getKeysByValue(targetValue: Long): List<String> {
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val keysWithMatchingValue = mutableListOf<String>()

        // 모든 항목 불러오기
        val allEntries = pref.all

        // 값이 일치하는 키 찾기
        for ((key, value) in allEntries) {
            if (value is Long && value == targetValue) {
                keysWithMatchingValue.add(key)
            }
        }

        Log.d("customTag", "keysWithMatchingValue: $keysWithMatchingValue")

        return keysWithMatchingValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getSerializable(ARG_DATE) as LocalDate
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AnalysisViewpagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appController = requireActivity().application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())
        cateRepository = CateRepository(mainDb.cateDao())

        var cateList: Flow<List<CateDb>>

        //카테고리 설정
        uiScope.launch {
            withContext(Dispatchers.IO) {
                cateList = cateRepository.getCateByInEx(0)
            }

            cateList.collect { list ->
                list.forEach { cate ->
                    val analcate = loadCategory(cate.cateId)
                    //카테고리 기초 세팅
                    if (analcate == -1L) {
                        if (cate.name == "기타" || cate.name == "경조사/선물") {
                            saveCategory(cate.cateId, 9)
                        } else if (cate.name == "식비") {
                            saveCategory(cate.cateId, 0)
                        } else if (cate.name == "미용" || cate.name == "의류") {
                            saveCategory(cate.cateId, 1)
                        } else if (cate.name == "카페/음료" || cate.name == "음주") {
                            saveCategory(cate.cateId, 2)
                        } else if (cate.name == "교통/차량") {
                            saveCategory(cate.cateId, 3)
                        } else if (cate.name == "의료" || cate.name == "보험료" || cate.name == "운동") {
                            saveCategory(cate.cateId, 4)
                        } else if (cate.name == "주거" || cate.name == "공과금") {
                            saveCategory(cate.cateId, 5)
                        } else if (cate.name == "교육") {
                            saveCategory(cate.cateId, 6)
                        } else if (cate.name == "문화/취미" || cate.name == "여행" || cate.name == "구독비") {
                            saveCategory(cate.cateId, 7)
                        } else if (cate.name == "마트/생필품" || cate.name == "통신비") {
                            saveCategory(cate.cateId, 8)
                        }
                    }
                }
                setupPieChart()
            }
        }

        pieChart = binding.pieChart

        //파이 차트 클릭 리스너
        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    val index = entries.indexOf(e)
                    if (index != -1) {
                        categoryDialog(index.toLong())
                    }
                }
            }

            override fun onNothingSelected() {
            }
        })

        //커스텀 범례 클릭 리스너
        binding.cate0.setOnClickListener {
            categoryDialog(0L)
        }

        binding.cate1.setOnClickListener {
            categoryDialog(1L)
        }

        binding.cate2.setOnClickListener {
            categoryDialog(2L)
        }

        binding.cate3.setOnClickListener {
            categoryDialog(3L)
        }

        binding.cate4.setOnClickListener {
            categoryDialog(4L)
        }

        binding.cate5.setOnClickListener {
            categoryDialog(5L)
        }

        binding.cate6.setOnClickListener {
            categoryDialog(6L)
        }

        binding.cate7.setOnClickListener {
            categoryDialog(7L)
        }

        binding.cate8.setOnClickListener {
            categoryDialog(8L)
        }

        binding.cate9.setOnClickListener {
            categoryDialog(9L)
        }

    }

    //카테고리 또는 파이 차트 클릭 다이얼로그
    private fun categoryDialog(cateIndex: Long) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.analysis_cate_dialog, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        Log.d("customTag", "cateIndex: $cateIndex")

        //인덱스에 맞는 데이터 불러오기
        val cateIndexKeys = getKeysByValue(cateIndex)

        //리사이클러 뷰
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.analRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //키가 없으면
        if (cateIndexKeys.isEmpty()) {
            dialogView.findViewById<TextView>(R.id.noRecy).visibility = View.VISIBLE
            dialogView.findViewById<ScrollView>(R.id.recyLayout).visibility = View.GONE
        } else {
            val moneyDatas = mutableListOf<MoneyAndCate>()

            val (firstDayMillis, lastDayMillis) = getMonthRangeMillis(date)


            uiScope.launch {

                withContext(Dispatchers.IO) {
                    cateIndexKeys.forEach { key ->
                        moneyRepository.getAnalCate(firstDayMillis, lastDayMillis, key.toLong()).forEach { money ->
                            moneyDatas.add(money)
                        }
                    }
                }

                Log.d("customTag", "moneyDatas: $moneyDatas")

                withContext(Dispatchers.Main) {
                    dialogView.findViewById<TextView>(R.id.cateText).text = getCategoryLabel(cateIndex.toInt())
                    if (moneyDatas.isEmpty()) {
                        dialogView.findViewById<TextView>(R.id.noRecy).visibility = View.VISIBLE
                        dialogView.findViewById<ScrollView>(R.id.recyLayout).visibility = View.GONE
                    } else {
                        recyclerView.adapter = AnalCateAdapter(moneyDatas)
                    }
                }
            }

            //닫기 버튼
            dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
                dialog.dismiss()
            }
        }


        dialog.show()

    }

    // Pie 차트 세팅
    private fun setupPieChart() {
        val (firstDayMillis, lastDayMillis) = getMonthRangeMillis(date)

        uiScope.launch {
            val moneyList = withContext(Dispatchers.IO) {
                moneyRepository.getMoneyMonthData(firstDayMillis, lastDayMillis)
            }

            if (moneyList.isEmpty()) {
                // 데이터가 없을 때 UI 처리
                withContext(Dispatchers.Main) {
                    pieChart.visibility = View.GONE
                    binding.noPieChart.visibility = View.VISIBLE
                }
                return@launch  // 데이터가 없으면 이후 작업을 진행하지 않음
            }

            // 데이터가 있을 때의 UI 처리
            withContext(Dispatchers.Main) {
                pieChart.visibility = View.VISIBLE
                binding.noPieChart.visibility = View.GONE
            }

            val cateMoney: MutableList<Pair<Long, Long>> = mutableListOf() // 아이디, 총액
            val moneys: MutableList<Long> = MutableList(10) { 0L } // 카테고리 분류해서 더한 리스트

            withContext(Dispatchers.IO) {
                var index = moneyList[0].moneyDb.cateId
                var sum = 0L

                moneyList.forEach { money ->
                    if (money.moneyDb.cateId != index) {
                        cateMoney.add(Pair(index, sum))
                        index = money.moneyDb.cateId
                        sum = money.moneyDb.money
                    } else {
                        sum += money.moneyDb.money
                    }
                }

                // 마지막 누적된 데이터 추가
                cateMoney.add(Pair(index, sum))

                cateMoney.forEach { money ->
                    val cateIndex = loadCategory(money.first)

                    when (cateIndex) {
                        in 0L..9L -> moneys[cateIndex.toInt()] += money.second
                    }
                }
            }

            // Pie 차트 업데이트
            withContext(Dispatchers.Main) {

                moneys.forEachIndexed { index, money ->
                    when (index) {
                        0 -> binding.index0.text = money.toString()
                        1 -> binding.index1.text = money.toString()
                        2 -> binding.index2.text = money.toString()
                        3 -> binding.index3.text = money.toString()
                        4 -> binding.index4.text = money.toString()
                        5 -> binding.index5.text = money.toString()
                        6 -> binding.index6.text = money.toString()
                        7 -> binding.index7.text = money.toString()
                        8 -> binding.index8.text = money.toString()
                        9 -> binding.index9.text = money.toString()
                    }
                }

                entries = ArrayList()
                moneys.forEachIndexed { index, money ->
                    Log.d("PieChartData", "Category $index: $money")
                    entries.add(PieEntry(money.toFloat(), getCategoryLabel(index)))
                }

                val dataSet = PieDataSet(entries, "Categories")
                dataSet.setDrawValues(false) //값 표시 비활
                pieChart.setDrawEntryLabels(false) //레이블 표시 비활
                pieChart.description.isEnabled = false //설명 표시 비활
                pieChart.legend.isEnabled = false //범례 표시 비활

                dataSet.setColors(
                    resources.getColor(R.color.pink, null),
                    resources.getColor(R.color.mint, null),
                    resources.getColor(R.color.yellow2, null),
                    resources.getColor(R.color.orange, null),
                    resources.getColor(R.color.green2, null),
                    resources.getColor(R.color.blue1, null),
                    resources.getColor(R.color.purple1, null),
                    resources.getColor(R.color.yellow, null),
                    resources.getColor(R.color.green3, null),
                    resources.getColor(R.color.gray, null)
                )

                val data = PieData(dataSet)
                pieChart.data = data
                pieChart.invalidate()
            }
        }
    }

    private fun getCategoryLabel(index: Int): String {
        return when (index) {
            0 -> "식비"
            1 -> "패션/미용"
            2 -> "음료/주류"
            3 -> "교통"
            4 -> "의료/건강"
            5 -> "주거"
            6 -> "교육"
            7 -> "여가"
            8 -> "생활"
            9 -> "기타"
            else -> "기타"
        }
    }

    // 특정 월의 첫 번째 날과 마지막 날을 밀리초 단위로 반환하는 함수
    private fun getMonthRangeMillis(yearMonth: LocalDate): Pair<Long, Long> {
        // 첫 번째 날과 마지막 날 계산
        val firstDay = LocalDate.of(yearMonth.year, yearMonth.month, 1)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())

        // UTC 기준으로 변환
        val zoneId = ZoneId.of("UTC")

        // LocalDate를 Date로 변환
        val firstDayMillis = Date.from(firstDay.atStartOfDay(zoneId).toInstant()).time
        val lastDayMillis = Date.from(lastDay.atTime(23, 59, 59, 999).atZone(zoneId).toInstant()).time

        return Pair(firstDayMillis, lastDayMillis)
    }


    //정적 멤버
    companion object {
        private const val ARG_DATE = "date"

        //프래그먼트 생성자
        @JvmStatic
        fun newInstance(date: LocalDate) =
            AnalFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }


}