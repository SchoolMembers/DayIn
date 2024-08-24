package com.schedule.dayin.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.MainActivity
import com.schedule.dayin.R
import com.schedule.dayin.databinding.FragmentMBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.dayin.AppController
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.views.CateAdapter
import com.schedule.dayin.views.UserCateAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoneyFragment : Fragment() {

    private var _binding: FragmentMBinding? = null
    private val binding get() = _binding!!

    var currentDayViewContainer: DayViewContainer? = null

    private lateinit var appController : AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var calendar = Calendar.getInstance()

    private var cateList: Flow<List<CateDb>> = emptyFlow()
    private lateinit var cateRepository: CateRepository

    private lateinit var cateAdapter: CateAdapter

    //선택된 카테고리
    private var selectedCate: CateDb? = null
    private var cateId: Long = -1L

    private var inEx = 0
    private lateinit var cateRecyclerView: RecyclerView

    //선택된 삭제할 카테고리
    private var delCates: List<CateDb>? = null
    private lateinit var delCheckButton: TextView

    //사용자 카테고리
    private lateinit var userCateRecyclerView: RecyclerView

    private lateinit var userCateAdapter: UserCateAdapter

    private var userCateList: Flow<List<MoneyAndCate>> = emptyFlow()

    private var userCateListBefore: Flow<List<CateDb>> = emptyFlow()

    //프래그먼트 뷰를 생성하고 초기화. 프래그먼트의 레이아웃 인플레이트 -> 뷰 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMBinding.inflate(inflater, container, false)
        Log.d("customTag", "MoneyFragment onCreateView called")
        return binding.root
    }

    //onCreateView() 완료 후 호출. 프래그먼트 뷰 생성 이후 추가적인 초기화 작업. (뷰 관련 로직 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appController = requireActivity().application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())
        cateRepository = CateRepository(mainDb.cateDao())


        //애니메이션 비활성화
        with(binding.calendarView) {
            itemAnimator = null
        }

        //연, 월 표시 공간
        val barDateYear = (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)

        //kizitonwose calendar
        val calendarView = binding.calendarView
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(100)
        val lastMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek


        //시작 월, 종료 월, 첫 주의 요일
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.Rectangle

        Log.d("customTag", "MoneyFragment onViewCreated called; day setup complete")


        //캘린더의 각 날짜 뷰 정의
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString() //dayText에 날짜 표시
                if (day.position == DayPosition.MonthDate) { //현재 날짜가 현재 월 내에 있을 때
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }

                container.view.setOnClickListener {
                    currentDayViewContainer = container
                    showDateDialog(day)

                    Log.d("customTag", "MoneyFragment onViewCreated called; day clicked")
                }

                container.click.setOnClickListener {
                    currentDayViewContainer = container
                    showDateDialog(day)

                    Log.d("customTag", "MoneyFragment onViewCreated called; day clicked")
                }

            }
        }

        //스크롤 리스너 ( 월 스크롤 -> 연, 월 업데이트)
        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                barDateYear?.text = p1.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
                Log.d("customTag", "MoneyFragment onViewCreated called; barDateYear updated")
            }
        }

        // barDateYear 클릭 리스너 설정
        barDateYear?.setOnClickListener {
            showYearMonthPicker(calendarView, currentMonth)
        }

    }

    // 연도 및 월 선택 다이얼로그 보여주기
    private fun showYearMonthPicker(calendarView: com.kizitonwose.calendar.view.CalendarView, currentMonth: YearMonth) {
        // 다이얼로그 뷰 생성
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_month_picker, null)
        val yearEdit: EditText = dialogView.findViewById(R.id.year)
        val monthEdit: EditText = dialogView.findViewById(R.id.month)
        val confirmButton: Button = dialogView.findViewById(R.id.confirm_button)

        // 최소, 최대 날짜 계산
        val minYearMonth = currentMonth.minusMonths(98)
        val maxYearMonth = currentMonth.plusMonths(99)

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
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

        confirmButton.setOnClickListener {
            //int 전환
            val selectedYear = if (yearText == "") 0 else yearText.toInt()
            val selectedMonth = if (monText == "") 0 else monText.toInt()

            //아무 값도 입력하지 않았을 때
            if (selectedYear == 0 || selectedMonth == 0) {
                Toast.makeText(requireContext(), "연도와 월을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //12 초과
            if (selectedMonth > 12) {
                Toast.makeText(requireContext(), "월은 12월 이하여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 등록할 날짜 세팅
            val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)

            Log.d("customTag", "Selected Year: $selectedYear, Selected Month: $selectedMonth")
            Log.d("customTag", "maxMon: $maxYearMonth, minMon: $minYearMonth")


            // 범위 초과 또는 미달 시 토스트 메시지 표시
            if ((selectedYear <= minYearMonth.year && selectedMonth < minYearMonth.monthValue)  || (selectedYear >= maxYearMonth.year && selectedMonth > maxYearMonth.monthValue)) {
                Toast.makeText(
                    requireContext(),
                    "선택할 수 있는 날짜 범위는 ${minYearMonth.year}년 ${minYearMonth.monthValue}월 ~ ${maxYearMonth.year}년 ${maxYearMonth.monthValue}월입니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 선택한 연도와 월로 캘린더 스크롤
                calendarView.scrollToMonth(selectedYearMonth)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    //날짜 셀 클릭 다이얼로그
    private fun showDateDialog(day: CalendarDay) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_m, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        //날짜 설정
        setupDialog(dialogView, day)

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        //추가 버튼
        val addButton = dialogView.findViewById<TextView>(R.id.addButton)
        addButton.setOnClickListener {
            showAddDialog(day)
            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        dialog.show()
    }

    //추가 다이얼로그
    private fun showAddDialog(day: CalendarDay) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_m_add, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        //날짜 설정
        setupDialog(dialogView, day)

        //date 설정 (데이터베이스 등록)
        calendar = Calendar.getInstance()
        calendar.set(day.date.year, day.date.monthValue - 1, day.date.dayOfMonth) // 날짜 설정

        //체크 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.isEnabled = false
        checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)

        //카테고리 리사이클러 뷰
        inEx = 0
        cateRecyclerView = dialogView.findViewById<RecyclerView>(R.id.cateRecyclerView)
        cateRecyclerView.layoutManager = GridLayoutManager(context, 3)
        loadCate(inEx, cateRecyclerView)

        //닫기
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        //금액 입력
        val moneyTitle = dialogView.findViewById<TextView>(R.id.moneyText)
        var moneyText: String = ""

        moneyTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                moneyText = s?.toString() ?: ""
                Log.d("customTag", "MoneyFragment onViewCreated called; moneyText: $moneyText")

                if (moneyText == "" || moneyText == "0") {
                    checkButton.isEnabled = false
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)
                } else {
                    checkButton.isEnabled = true
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.save_icon, null)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //소비 지출 토글
        val cateToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.cateToggle)
        cateToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                inEx = when (checkedId) {
                    R.id.minus -> {
                        loadCate(0, cateRecyclerView)
                        Log.d("customTag", "MoneyFragment onViewCreated called; cateToggle checked minus")
                        0
                    }
                    R.id.plus -> {
                        loadCate(1, cateRecyclerView)
                        Log.d("customTag", "MoneyFragment onViewCreated called; cateToggle checked plus")
                        1
                    }
                    else -> 0
                }
                Log.d("customTag", "MoneyFragment onViewCreated called; cateToggle checked")
            }
        }

        //카테고리 추가 버튼
        val catePlus = dialogView.findViewById<Button>(R.id.catePlus)
        catePlus.setOnClickListener {
            addCateDialog()
            Log.d("customTag", "MoneyFragment onViewCreated called; catePlus clicked")
        }

        //카테고리 제거 버튼
        val cateDelete = dialogView.findViewById<Button>(R.id.cateMinus)
        cateDelete.setOnClickListener {
            deleteCateDialog()
            Log.d("customTag", "MoneyFragment onViewCreated called; cateDelete clicked")
        }

        //자동 등록
        var auto = 0
        val autoToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.autoToggle)

        val autoTitleLayout = dialogView.findViewById<LinearLayout>(R.id.autoTitleLayout)
        autoToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                auto = when (checkedId) {
                    R.id.autoDefault -> 0
                    R.id.autoWeek -> 1
                    R.id.autoMon -> 2
                    R.id.autoYear -> 3
                    else -> auto
                }
                Log.d("customTag", "auto value updated: $auto")
                if (auto != 0) {
                    autoTitleLayout.visibility = View.VISIBLE
                } else {
                    autoTitleLayout.visibility = View.GONE
                }
            }
        }

        //자동 등록 제목
        var autoText: String? = ""
        val autoTitle = dialogView.findViewById<EditText>(R.id.autoTitle)
        autoTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                autoText = s?.toString() ?: ""
                Log.d("customTag", "MoneyFragment onViewCreated called; autoText: $autoText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 자동 등록 info 리스너
        val infoButton1 = dialogView.findViewById<Button>(R.id.infoButton1)
        infoButton1.setOnClickListener {
            Toast.makeText(context, R.string.auto_money, Toast.LENGTH_SHORT).show()
            Log.d("customTag", "moneyFragment onViewCreated called; infoButton1 clicked")
        }

        //메모
        val memo = dialogView.findViewById<EditText>(R.id.memoText)
        var memoText: String? = ""
        memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memoText = s?.toString() ?: ""
                Log.d("customTag", "MoneyFragment onViewCreated called; memoText: $memoText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        //체크 버튼
        checkButton.setOnClickListener {
            if (autoText == "" && auto != 0) {
                Toast.makeText(context, "자동 등록 활성화 시 반드시 제목을 입력해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (memoText == "") {
                memoText = null
            }
            if (auto == 0) {
                autoText = null
            }

            if (moneyText == "" || moneyText == "0") {
                Toast.makeText(context, "금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cateId == -1L) {
                Toast.makeText(context, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //데이터베이스에 저장
            Log.d("customTag", "MoneyFragment onViewCreated called; checkButton clicked")
            dialog.dismiss()
            val moneyDate = calendar.time

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    moneyRepository.insertMoney(MoneyDb(
                        date = moneyDate,
                        money = moneyText.toInt(),
                        auto = auto,
                        memo = memoText,
                        title = autoText,
                        cateId = cateId
                    ))
                    cateId = -1
                }
            }
        }

        dialog.show()
    }

    // 다이얼로그 내의 뷰들을 참조해 날짜 정보 설정
    private fun setupDialog(dialogView: View, day: CalendarDay) {
        val monYearTextView = dialogView.findViewById<TextView>(R.id.monYear)
        monYearTextView.text = day.date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)").withLocale(Locale.KOREAN))
        Log.d("customTag", "ScheduleFragment onViewCreated called; monYearTextView updated")
    }

    //카테고리 데이터 로드
    private fun loadCate(inEx: Int, cateRecyclerView: RecyclerView) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                cateList = cateRepository.getCateByInEx(inEx)
                cateAdapter = CateAdapter(cateList, onDataChanged = { onCateDataChanged() })
                cateRecyclerView.adapter = cateAdapter
                Log.d("customTag", "MoneyFragment onViewCreated called; cateList updated")
            }
        }
    }

    //카테고리 변경 함수
    private fun onCateDataChanged() {
        selectedCate = cateAdapter.getSelectedCategory()
        cateId = selectedCate?.cateId ?: -1L
    }

    //카테고리 추가 다이얼로그
    private fun addCateDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_add, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        var addInEx = 0

        //체크 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.isEnabled = false
        checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)

        //카테고리 이름 입력
        val cateName = dialogView.findViewById<TextView>(R.id.editText)
        var cateText: String = ""

        cateName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cateText = s?.toString() ?: ""
                Log.d("customTag", "MoneyFragment onViewCreated called; moneyText: $cateText")

                if (cateText == "") {
                    checkButton.isEnabled = false
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)
                } else {
                    checkButton.isEnabled = true
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.save_icon, null)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //소비 지출 토글
        val cateToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.cateToggle)
        cateToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                addInEx = when (checkedId) {
                    R.id.minus -> 0
                    R.id.plus -> 1
                    else -> 0
                }
                Log.d("customTag", "MoneyFragment onViewCreated called; cateToggle checked")
            }
        }

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        //체크 버튼 클릭
        checkButton.setOnClickListener {
            if (cateText == "") {
                Toast.makeText(context, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    cateRepository.insertCate(CateDb(name = cateText, inEx = addInEx))
                    loadCate(inEx, cateRecyclerView)
                }
            }

            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; cate add checkButton clicked")
        }

        dialog.show()
    }

    //카테고리 삭제 다이얼로그
    private fun deleteCateDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_del, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val delDialog = dialogBuilder.create()

        //사용자 지정 리사이클러
        userCateRecyclerView = dialogView.findViewById(R.id.deleteRecyclerView)
        userCateRecyclerView.layoutManager = LinearLayoutManager(context)
        loadUserCate(userCateRecyclerView)

        //체크 버튼
        delCheckButton = dialogView.findViewById(R.id.checkButton)
        delCheckButton.isEnabled = false
        delCheckButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)

        //체크 버튼 클릭
        delCheckButton.setOnClickListener {
            val selectedCategories = delCates
            if (selectedCategories.isNullOrEmpty()) {
                Toast.makeText(context, "삭제할 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                delReCheck(selectedCategories, delDialog)
            }
        }

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            delDialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        delDialog.show()
    }

    //삭제 다시 확인
    private fun delReCheck(selectedCategories: List<CateDb>, delDialog: AlertDialog) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_del_re, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = dialogBuilder.create()

        //텍스트 설정
        val textView = dialogView.findViewById<TextView>(R.id.cateList)
        textView.text = delCates?.joinToString(", ") { it.name }

        //확인 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    selectedCategories.forEach { cate ->
                        moneyRepository.deleteMoneyByCateId(cate.cateId)
                    }
                    selectedCategories.forEach { cate ->
                        cateRepository.deleteCateById(cate.cateId)
                    }
                }
                withContext(Dispatchers.Main) {
                    loadCate(inEx, cateRecyclerView)
                    dialog.dismiss()
                    delCates = null

                    //카테고리 삭제 선택 다이얼로그도 종료
                    delDialog.dismiss()
                }
            }
        }

        //취소 버튼
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    //사용자 카테고리 데이터 로드
    private fun loadUserCate(userCateRecyclerView: RecyclerView) {
        uiScope.launch {
            userCateListBefore = cateRepository.getUserCate()
            userCateList = moneyRepository.getUserCate()
            userCateList.collect { list ->
                if (list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        userCateAdapter = UserCateAdapter(requireContext(), userCateListBefore, userCateList, onDataChanged = { cateDel() })
                        userCateRecyclerView.adapter = userCateAdapter
                        Log.d("customTag", "MoneyFragment loadUserCate: Adapter set with ${list.size} items.")
                    }
                } else {
                    Log.d("customTag", "MoneyFragment loadUserCate: userCateList is empty.")
                }
            }
        }
    }

    //카테고리 삭제 리스트 등록 함수
    private fun cateDel() {
        delCates = userCateAdapter.getSelectedCategory()

        //체크 버튼 활성/비활성
        if (delCates == null) {
            delCheckButton.isEnabled = false
            delCheckButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)
        } else {
            delCheckButton.isEnabled = true
            delCheckButton.background = ResourcesCompat.getDrawable(resources, R.drawable.save_icon, null)
        }
    }

    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    //정적 멤버
    companion object {
        fun newInstance(): MoneyFragment {
            val args = Bundle()
            val fragment = MoneyFragment()
            fragment.arguments = args
            return fragment
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val click: View = view.findViewById(R.id.clickLayout)
        val moneyRecyclerView: RecyclerView = view.findViewById(R.id.moneyRecyclerView)
    }
}