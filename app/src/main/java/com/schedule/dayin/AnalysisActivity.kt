package com.schedule.dayin

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AnalysisActivityBinding
import com.schedule.dayin.views.AnalManageAdapter
import com.schedule.dayin.views.AnalPagerAdapter
import com.schedule.dayin.views.AnalUserCateAdapter
import com.schedule.dayin.views.AutoAnalAdapter
import com.schedule.dayin.views.ItemDecoration
import com.schedule.dayin.views.UserCateAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AnalysisActivity: AppCompatActivity() {

    private lateinit var binding: AnalysisActivityBinding

    //텍스트 버튼 클릭 초기화
    private var autoManage = false
    private lateinit var autoTextView: TextView

    //데이터
    private lateinit var appController: AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private lateinit var cateRepository: CateRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    //뷰페이저
    private lateinit var pagerAdapter: AnalPagerAdapter

    //데이터 동기화
    private lateinit var recyclerViews: List<RecyclerView>
    private lateinit var dialogViewAnal: View

    // 0: 식비 1: 패션/미용 2: 음료/주류 3: 교통 4: 의료/건강 5: 주거 6: 교육 7: 여가 8: 생활 9: 기타
    private fun loadCategory(category: Long): Long {
        val cateString = category.toString()
        val pref: SharedPreferences = this.getSharedPreferences("anal", Activity.MODE_PRIVATE)
        return pref.getLong(cateString, -1L)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = AnalysisActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appController = this.application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())
        cateRepository = CateRepository(mainDb.cateDao())

        //날짜 표시
        val barDate = binding.monYear
        val currentMonth = LocalDate.now()
        barDate.text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))

        //뷰페이저
        pagerAdapter = AnalPagerAdapter(this, currentMonth)

        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.setCurrentItem(pagerAdapter.startingPosition, false)

        //페이지 전환 함수
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val offset = position - pagerAdapter.startingPosition
                val selectedDate = currentMonth.plusMonths(offset.toLong())
                barDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
            }
        })

        // 상단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainTopBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // 하단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        //하단 바 활성화 상태
        binding.bottomNavigation.selectedItemId = R.id.barMoney

        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //bottom navigation click event
        val memoIntent = Intent(this, MemoActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)
        val analysisIntent = Intent(this, AnalysisActivity::class.java)

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.barHome -> {
                    startActivity(homeIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click home button")
                    true
                }
                R.id.barMemo -> {
                    startActivity(memoIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click memo button")
                    true
                }
                R.id.barMoney -> {
                    startActivity(analysisIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click analysis button")
                    true
                }
                else -> false
            }
        }

        //고정 지출 클릭 리스너
        autoTextView = binding.autoManage
        autoTextView.setOnClickListener {
            if (!autoManage) {
                autoTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
                autoManage = true
                showAutoDialog()
                Log.d("customTag", "AnalysisActivity onCreate called; autoManage = true")
            }
        }

        //소비 분석 관리 클릭 리스너
        binding.cateManage.setOnClickListener {
            binding.cateManage.setTextColor(ContextCompat.getColor(this, R.color.black))
            cateDialog()
        }

        //날짜 클릭 리스너
        binding.monYear.setOnClickListener {
            showYearMonthPicker(YearMonth.now())
        }
    }

    // 연도 및 월 선택 다이얼로그 보여주기
    private fun showYearMonthPicker(currentMonth: YearMonth) {
        // 다이얼로그 뷰 생성
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_month_picker, null)
        val yearEdit: EditText = dialogView.findViewById(R.id.year)
        val monthEdit: EditText = dialogView.findViewById(R.id.month)
        val confirmButton: Button = dialogView.findViewById(R.id.confirm_button)

        // 최소, 최대 날짜 계산
        val minYearMonth = currentMonth.minusMonths(98)
        val maxYearMonth = currentMonth.plusMonths(99)

        // 다이얼로그 생성
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        //년
        var yearText: String = "0"
        yearEdit.hint = currentMonth.year.toString()
        yearEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                yearText = s?.toString() ?: "0"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //월
        var monText: String = "0"
        monthEdit.hint = currentMonth.monthValue.toString()
        monthEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                monText = s?.toString() ?: "0"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //체크 버튼
        confirmButton.setOnClickListener {
            //int 전환
            val selectedYear = if (yearText == "") 0 else yearText.toInt()
            val selectedMonth = if (monText == "") 0 else monText.toInt()

            //아무 값도 입력하지 않았을 때
            if (selectedYear == 0 || selectedMonth == 0) {
                Toast.makeText(this, "연도와 월을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //12 초과
            if (selectedMonth > 12) {
                Toast.makeText(this, "월은 12월 이하여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 등록할 날짜 세팅
            val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)

            Log.d("customTag", "Selected Year: $selectedYear, Selected Month: $selectedMonth")
            Log.d("customTag", "maxMon: $maxYearMonth, minMon: $minYearMonth")

            // 범위 초과 또는 미달 시 토스트 메시지 표시
            if ((selectedYear <= minYearMonth.year && selectedMonth < minYearMonth.monthValue) ||
                (selectedYear >= maxYearMonth.year && selectedMonth > maxYearMonth.monthValue)
            ) {
                Toast.makeText(
                    this,
                    "선택할 수 있는 날짜 범위는 ${minYearMonth.year}년 ${minYearMonth.monthValue}월 ~ ${maxYearMonth.year}년 ${maxYearMonth.monthValue}월입니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 선택한 연도와 월로 ViewPager 페이지 변경
                val offset = selectedYearMonth.monthValue - currentMonth.monthValue +
                        (selectedYearMonth.year - currentMonth.year) * 12
                binding.viewpager.setCurrentItem(pagerAdapter.startingPosition + offset, false)

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    //소비 분석 관리 다이얼로그
    private fun cateDialog() {
        dialogViewAnal = LayoutInflater.from(this).inflate(R.layout.dialog_anal_cate, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogViewAnal)
        val dialog = dialogBuilder.create()


        Log.d("customTag", "AnalysisActivity onCreate called; cateDialog called")

        //닫기 버튼 클릭 리스너
        val close = dialogViewAnal.findViewById<Button>(R.id.closeButton)
        close.setOnClickListener {
            binding.cateManage.setTextColor(ContextCompat.getColor(this, R.color.gray))
            dialog.dismiss()
        }


        //추가 버튼 리스너
        val addButton = dialogViewAnal.findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            showCateDialog {
                cateAnalDataLoad(dialogViewAnal, dialog) // 데이터 콜백
            }
        }

        //다이얼로그가 닫히면
        dialog.setOnDismissListener {
            binding.cateManage.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
        cateAnalDataLoad(dialogViewAnal, dialog)
    }

    //소비 분석 관리 다이얼로그 데이터 로드
    private fun cateAnalDataLoad(dialogView: View, dialog: AlertDialog) {
        recyclerViews = listOf(
            R.id.recyclerView0,
            R.id.recyclerView1,
            R.id.recyclerView2,
            R.id.recyclerView3,
            R.id.recyclerView4,
            R.id.recyclerView5,
            R.id.recyclerView6,
            R.id.recyclerView7,
            R.id.recyclerView8,
            R.id.recyclerView9
        ).map { id -> dialogView.findViewById(id) }

        recyclerViews.forEach { it.layoutManager = LinearLayoutManager(this) }

        val cateLists = List(10) { mutableListOf<CateDb>() }

        uiScope.launch {
            val cateAll: Flow<List<CateDb>>
            withContext(Dispatchers.IO) {
                cateAll = cateRepository.getCateByInEx(0)
            }

            cateAll.collect { cateList ->
                cateList.forEach { cate ->
                    val analKey = loadCategory(cate.cateId)
                    cateLists.getOrNull(analKey.toInt())?.add(cate)
                }

                withContext(Dispatchers.Main) {
                    recyclerViews.forEachIndexed { index, recyclerView ->
                        recyclerView.adapter = AnalManageAdapter(cateLists[index])
                    }
                    dialog.show()
                }
            }
        }
    }

    //카테고리 인덱스 추가 다이얼로그
    private fun showCateDialog(onDataChanged: (View) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.analysis_user_cate, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        Log.d("customTag", "AnalysisActivity onCreate called; showCateDialog called")

        //닫기 버튼
        val close = dialogView.findViewById<Button>(R.id.closeButton)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val recyclerViews = dialogView.findViewById<RecyclerView>(R.id.addRecyclerView)
        recyclerViews.layoutManager = LinearLayoutManager(this)

        //리사이클러 뷰 데코레이션 지정
        val verticalSpaceHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics
        ).toInt()
        recyclerViews.addItemDecoration(ItemDecoration(verticalSpaceHeight))

        uiScope.launch {
            val userCate = userCateLoad()
            Log.d("customTag", "AnalysisActivity onCreate called; userCate: $userCate")
            withContext(Dispatchers.Main) {
                val adapter: AnalUserCateAdapter
                if (userCate.isNotEmpty()) {
                    adapter = AnalUserCateAdapter(this@AnalysisActivity, userCate) {
                        onDataChanged(dialogViewAnal)
                        updateData()

                    }
                    recyclerViews.adapter = adapter
                    Log.d("customTag", "AnalysisActivity onCreate called; adapter: $adapter")
                } else {
                    dialogView.findViewById<ScrollView>(R.id.recyLayout).visibility = View.GONE
                    dialogView.findViewById<TextView>(R.id.noLayout).visibility = View.VISIBLE
                }
                dialog.show()
            }


        }
    }
    private fun updateData() {
        pagerAdapter.updateData()
    }

    private suspend fun userCateLoad(): List<CateDb> {
        return withContext(Dispatchers.IO) {
            try {
                val userCate = cateRepository.getUserCateInex(0)
                Log.d("customTag", "AnalysisActivity userCateLoad called; userCate: $userCate")
                userCate
            } catch (e: Exception) {
                Log.e("customTag", "Error retrieving user categories", e)
                emptyList()
            }
        }
    }


    //고정 지출 다이얼로그
    private fun showAutoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anal_auto, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        //info 버튼 클릭 리스너
        val info = dialogView.findViewById<Button>(R.id.infoButton)
        info.setOnClickListener {
            Toast.makeText(this, "자동 등록 된 데이터들 입니다! 메뉴의 자동 등록 관리에서 관리할 수 있어요.", Toast.LENGTH_SHORT).show()
        }

        //닫기 버튼 클릭 리스너
        val close = dialogView.findViewById<Button>(R.id.closeButton)
        close.setOnClickListener {
            dialog.dismiss()
            autoManage = false
            autoTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        //리사이클러 뷰 데코레이션 지정
        val verticalSpaceHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        ).toInt()

        val autoRecyclerView = dialogView.findViewById<RecyclerView>(R.id.autoRecyclerView)
        autoRecyclerView.layoutManager = LinearLayoutManager(this)

        autoDataLoad(autoRecyclerView)

        autoRecyclerView.addItemDecoration(ItemDecoration(verticalSpaceHeight))






        dialog.setOnDismissListener {
            autoManage = false
            autoTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        dialog.show()
    }

    //고정 지출 데이터 불러오는 함수
    private fun autoDataLoad(autoRecyclerView: RecyclerView) {
        val autoList= mutableListOf<MoneyAndCate>()

        var title = ""

        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoMoney().forEach { money ->
                        if (title != money.moneyDb.title) {
                            title = money.moneyDb.title!!
                            autoList.add(money)
                        }
                    }
                }
                withContext(Dispatchers.Main){
                    autoRecyclerView.adapter = AutoAnalAdapter(autoList)
                }
            } catch (e: Exception) {
                Log.e("AutoData", "Error collecting auto data", e)
                autoList.clear()
            }

        }

    }

}