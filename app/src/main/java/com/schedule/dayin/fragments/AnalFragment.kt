package com.schedule.dayin.fragments

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AnalysisViewpagerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    //카테고리 분류 저장
    fun saveCategory(category: Long, index: Long) {
        val cateString = category.toString()
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putLong(cateString, index)
        editor.apply()
    }

    // 0: 식비 1: 패션/미용 2: 음료/주류 3: 교통 4: 의료/건강 5: 주거 6: 교육 7: 여가 8: 생활 9: 기타
    fun loadCategory(category: Long): Long {
        val cateString = category.toString()
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        return pref.getLong(cateString, -1L)
    }

    fun removeCategory(category: Long) {
        val cateString = category.toString()
        val pref: SharedPreferences = requireContext().getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.remove(cateString)
        editor.apply()
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
                        if (cate.cateId >= 25) {
                            saveCategory(cate.cateId, 9)
                        } else if (cate.name == "기타" || cate.name == "경조사/선물") {
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

    }

    // Pie 차트 세팅
    private fun setupPieChart() {
        val (firstDayMillis, lastDayMillis) = getMonthRangeMillis(date)

        pieChart = binding.pieChart

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
                        else -> {
                            moneys[9] += money.second
                            saveCategory(money.first, 9)
                        }
                    }
                }
            }

            // Pie 차트 업데이트
            withContext(Dispatchers.Main) {
                val entries = ArrayList<PieEntry>()
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