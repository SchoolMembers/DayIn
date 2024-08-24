package com.schedule.dayin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.schedule.dayin.AppController
import com.schedule.dayin.MainActivity
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.ScheduleRepository
import com.schedule.dayin.databinding.FragmentSBinding
import com.schedule.dayin.views.ScheduleAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import kotlin.coroutines.CoroutineContext
import com.schedule.dayin.views.ItemDecoration
import java.time.LocalTime
import java.time.ZoneId


class ScheduleFragment : Fragment(), CoroutineScope {

    private var _binding: FragmentSBinding? = null
    private val binding get() = _binding!!

    private lateinit var appController: AppController

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var currentDayViewContainer: DayViewContainer? = null

    var loadContainer: DayViewContainer? = null

    //데이터 체크
    private var clickCheck = false

    private lateinit var adapter: ScheduleAdapter

    private var dataList = mutableListOf<ScheduleDb>()




    //중요 날짜 데이터 불러오는 함수
    fun saveImportantDates(date: LocalDate) {
        //저장 객체
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val pref: SharedPreferences = requireContext().getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putInt(dateString, 0)
        editor.apply()
    }

    fun checkImportantDates(date: LocalDate): Boolean {
        val pref: SharedPreferences =  requireContext().getSharedPreferences("pref", Activity.MODE_PRIVATE)

        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val check = pref.getInt(dateString, -1)

        return check != -1

    }

    fun removeImportantDates(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val pref: SharedPreferences = requireContext().getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.remove(dateString)
        editor.apply()
    }




    //database
    private lateinit var mainDb: MainDatabase
    private lateinit var scheduleRepository: ScheduleRepository

    private var calendar = Calendar.getInstance()

    //시간 등록
    private var time = 0

    //알림 설정 여부
    private var noti: Int = 0

    //메모
    private var memoText: String? = ""

    private var maxVisibleItems: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job() // Job 초기화
    }

    //프래그먼트 뷰를 생성하고 초기화. 프래그먼트의 레이아웃 인플레이트 -> 뷰 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSBinding.inflate(inflater, container, false)
        Log.d("customTag", "ScheduleFragment onCreateView called")

        return binding.root
    }

    //onCreateView() 완료 후 호출. 프래그먼트 뷰 생성 이후 추가적인 초기화 작업. (뷰 관련 로직 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //애니메이션 비활성화
        with(binding.calendarView) {
            itemAnimator = null
        }

        //연, 월 표시 공간
        val barDateYear = (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)

        //kizitonwose calendar

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(100)
        val lastMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        /*val firstDayOfWeekNumber = firstDayOfWeek.value 일요일은 1, 토요일은 7
        임시 저장*/


        //시작 월, 종료 월, 첫 주의 요일
        val calendarView = binding.calendarView
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.Rectangle

        Log.d("customTag", "ScheduleFragment onViewCreated called; day setup complete")


        //database setting
        appController = requireActivity().application as AppController
        mainDb = appController.mainDb
        scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())

        Log.d("customTag", "ScheduleFragment onViewCreated called; database setting complete")



        //캘린더의 각 날짜 뷰 정의
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString() // 날짜 표시

                daySet(container, day)

                // 비동기로 데이터 로드
                dataLoad(container, day)


                container.view.setOnClickListener {
                    currentDayViewContainer = container
                    loadContainer = container
                    showDateDialog(day)
                }

                container.click.setOnClickListener {
                    currentDayViewContainer = container
                    loadContainer = container
                    showDateDialog(day)
                }

            }
        }




        //스크롤 리스너 ( 월 스크롤 -> 연, 월 업데이트)
        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                barDateYear?.text = p1.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
                Log.d("customTag", "ScheduleFragment onViewCreated called; barDateYear updated")

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


    //리사이클러 비동기 데이터 로그 함수
    fun dataLoad(container: DayViewContainer, day: CalendarDay) {
        uiScope.launch {
            dataList = loadScheduleDataForDay(day)

            val itemViewHeight = 27 // dp
            val itemViewHeightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                itemViewHeight.toFloat(),
                requireContext().resources.displayMetrics
            ).toInt()

            val paddingMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                37.toFloat(),
                requireContext().resources.displayMetrics
            ).toInt()

            val recyclerViewHeight = container.view.height

            maxVisibleItems = if (itemViewHeightPx != 0) {
                (recyclerViewHeight - paddingMargin) / itemViewHeightPx
            } else {
                0
            }

            if (dataList.isNotEmpty()) {
                if (container.scheduleRecyclerView.adapter == null) {
                    adapter = ScheduleAdapter(requireContext(), dataList, clickCheck, appController, day, maxVisibleItems)
                    container.scheduleRecyclerView.adapter = adapter
                    container.scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
                } else {
                    (container.scheduleRecyclerView.adapter as ScheduleAdapter).updateData(dataList)
                }
            } else {
                container.scheduleRecyclerView.adapter = null
            }

        }
    }



    //리사이클러 데이터 세팅
    private suspend fun loadScheduleDataForDay(day: CalendarDay): MutableList<ScheduleDb> {
        val date = day.date
        val dataList = mutableListOf<ScheduleDb>()

        val startDate = date.atTime(LocalTime.MIN)
        val endDate = date.atTime(LocalTime.MAX)

        val startZoneTime = startDate.atZone(ZoneId.systemDefault())
        val endZoneTime = endDate.atZone(ZoneId.systemDefault())

        withContext(Dispatchers.IO) {
            try {
                scheduleRepository.getTimes(
                    startZoneTime.toInstant().toEpochMilli(),
                    endZoneTime.toInstant().toEpochMilli()
                ).forEach { schedule ->
                    dataList.add(schedule)
                }
            } catch (e: Exception) {
                Log.e("ScheduleData", "Error collecting schedules", e)
            }
        }

        Log.d("ScheduleData", "Date: $date, DataList: $dataList")

        return dataList
    }


    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
        job.cancel() // 코루틴 취소
    }

    // 다이얼로그 내의 뷰들을 참조해 날짜 정보 설정
    private fun setupDialog(dialogView: View, day: CalendarDay) {
        val monYearTextView = dialogView.findViewById<TextView>(R.id.monYear)
        monYearTextView.text = day.date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)").withLocale(Locale.KOREAN))
        Log.d("customTag", "ScheduleFragment onViewCreated called; monYearTextView updated")
    }

    //날짜 클릭 다이얼로그 메서드
    private fun showDateDialog(day: CalendarDay) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_s, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        clickCheck = true

        //리사이클러
        val recyclerViewInDialog: RecyclerView = dialogView.findViewById(R.id.scheduleRecyclerView)

        uiScope.launch {
            dataList = loadScheduleDataForDay(day)
            if (dataList.isNotEmpty()) {
                adapter = ScheduleAdapter(requireContext(), dataList, clickCheck, appController, day, maxVisibleItems) {
                    dataLoad(currentDayViewContainer!!, day)
                }
                recyclerViewInDialog.adapter = adapter
                recyclerViewInDialog.layoutManager = LinearLayoutManager(context)
            } else {
                recyclerViewInDialog.adapter = null
            }
        }


        //리사이클러 뷰 데코레이션 지정
        val verticalSpaceHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        ).toInt()

        recyclerViewInDialog.addItemDecoration(ItemDecoration(verticalSpaceHeight))

        //중요 버튼 초기 설정
        val starButton = dialogView.findViewById<Button>(R.id.starButton)
        starButton.background = if (checkImportantDates(day.date)) {
            ResourcesCompat.getDrawable(resources, R.drawable.star_icon, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.star_border_icon, null)
        }

        //날짜 설정
        setupDialog(dialogView, day)

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; dialog closed")
            clickCheck = false
        }

        // 추가 버튼에 대한 클릭 리스너
        val addButton = dialogView.findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            showAddDialog(day)
            dialog.dismiss()
            clickCheck = false
        }

        //중요(별표) 버튼에 대한 리스너

        starButton.setOnClickListener {
            val container = currentDayViewContainer
            if (checkImportantDates(day.date)) {
                removeImportantDates(day.date)
                starButton.background = ResourcesCompat.getDrawable(resources, R.drawable.star_border_icon, null)
                Toast.makeText(context, R.string.star_cancel, Toast.LENGTH_SHORT).show()
                Log.d("customTag", "ScheduleFragment onViewCreated called; starButton clicked")
            } else {
                saveImportantDates(day.date)
                starButton.background = ResourcesCompat.getDrawable(resources, R.drawable.star_icon, null)
                Toast.makeText(context, R.string.star, Toast.LENGTH_SHORT).show()
                Log.d("customTag", "ScheduleFragment onViewCreated called; starButton cancel")
            }
            if (container != null) {
                daySet(container, day)
            }
        }

        //다이얼로그가 닫혔을 때
        dialog.setOnDismissListener {
            clickCheck = false
        }

        // 다이얼로그 표시
        dialog.show()
    }

    private fun daySet(container: DayViewContainer, day: CalendarDay) {
        val color = ContextCompat.getColor(requireContext(), R.color.pink)
        if (day.position == DayPosition.MonthDate) { //현재 날짜가 현재 월 내에 있을 때
            if (checkImportantDates(day.date)) {
                container.textView.setTextColor(color)
            } else {
                container.textView.setTextColor(Color.BLACK)
            }

        } else {
            if (checkImportantDates(day.date)) { //현재 날짜가 현재 월 내에 없을 때
                container.textView.setTextColor(color)
            } else {
                container.textView.setTextColor(Color.GRAY)
            }
        }
    }

    private fun showAddDialog(day: CalendarDay) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_s_add, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        noti = 0
        time = 0

        //날짜 설정
        setupDialog(dialogView, day)

        //date 설정
        calendar = Calendar.getInstance()
        calendar.set(day.date.year, day.date.monthValue - 1, day.date.dayOfMonth) // 날짜 설정

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            noti = 0
            time = 0
            Log.d("customTag", "ScheduleFragment onViewCreated called; dialog closed")
        }

        //제목 입력 저장
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleText)
        var titleText: String = "제목 없음"

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                titleText = s?.toString() ?: "제목 없음"
                Log.d("customTag", "ScheduleFragment onViewCreated called; titleText: $titleText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //색상 설정 리스너
        //colorDefault(lightGray) : 0 | colorGray : 1 | colorYellow : 2 | colorPurple : 3 | colorBlue : 4 | colorGreen 5
        var color = 0
        val colorButton0 = dialogView.findViewById<Button>(R.id.colorDefault)
        colorButton0.setOnClickListener {
            color = 0
            Toast.makeText(context, "회색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated lightGray")
        }

        val colorButton1 = dialogView.findViewById<Button>(R.id.colorGray)
        colorButton1.setOnClickListener {
            color = 1
            Toast.makeText(context, "검정색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated gray")
        }

        val colorButton2 = dialogView.findViewById<Button>(R.id.colorYellow)
        colorButton2.setOnClickListener {
            color = 2
            Toast.makeText(context, "노란색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated yellow")
        }

        val colorButton3 = dialogView.findViewById<Button>(R.id.colorPurple)
        colorButton3.setOnClickListener {
            color = 3
            Toast.makeText(context, "보라색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated purple")
        }

        val colorButton4 = dialogView.findViewById<Button>(R.id.colorBlue)
        colorButton4.setOnClickListener {
            color = 4
            Toast.makeText(context, "파란색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated blue")
        }

        val colorButton5 = dialogView.findViewById<Button>(R.id.colorGreen)
        colorButton5.setOnClickListener {
            color = 5
            Toast.makeText(context, "초록색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated green")
        }

        val colorButton6 = dialogView.findViewById<Button>(R.id.colorRed)
        colorButton6.setOnClickListener {
            color = 6
            Toast.makeText(context, "빨간색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated red")
        }


        // autoToggle 리스너
        var auto = 0

        val autoToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.autoToggle)
        autoToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                auto = when (checkedId) {
                    R.id.autoDefault -> 0
                    R.id.autoWeek -> 1
                    R.id.autoMon -> 2
                    R.id.autoYear -> 3
                    else -> auto // 기본값 유지
                }
                Log.d("customTag", "auto value updated: $auto")
            }
        }

        // autoToggle info 리스너
        val infoButton1 = dialogView.findViewById<Button>(R.id.infoButton1)
        infoButton1.setOnClickListener {
            Toast.makeText(context, R.string.auto_info1, Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; infoButton1 clicked")
        }

        //시간 등록 활성화 버튼
        val timeSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.timeSwitch)

        val timeHourEditText = dialogView.findViewById<EditText>(R.id.timeHour)
        val timeMinEditText = dialogView.findViewById<EditText>(R.id.timeMin)


        // 체크 버튼
        val checkButton = dialogView.findViewById<Button>(R.id.checkButton)
        checkButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; check button clicked")
            val scheduleDate = calendar.time


            //데이터 저장(일정 추가)
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    if (memoText == "") {
                        memoText = null
                    }

                    scheduleRepository.insertSche(
                        ScheduleDb(
                            date = scheduleDate,
                            title = titleText,
                            auto = auto,
                            notify = noti,
                            memo = memoText,
                            check = 0,
                            time = time,
                            color = color
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    val container = loadContainer
                    dataLoad(container!!, day)
                }
            }

            Log.d("customTag", "ScheduleFragment onViewCreated called; data saved")
        }


        //시간 등록 활성화 이벤트
        val timeLayout = dialogView.findViewById<View>(R.id.timeLayout)


        val textWatcher: TextWatcher? = null

        var timeHourText = 0
        var timeMinText = 0

        val errorMessage = requireContext().getString(R.string.value_error)

        val notifyToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.notify)


        fun updateTextField(editText: EditText, value: Int, max: Int, errorMessage: String) {
            if (value >= max) {
                try {
                    editText.removeTextChangedListener(textWatcher) // 무한 루프 방지
                    editText.setText("")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }  catch(e: Exception) {
                    editText.addTextChangedListener(textWatcher) // 리스너 재등록
                }
            }
        }

        // 시간 EditText에 대한 공통 TextWatcher 생성 함수
        fun createTextWatcher(isHourEditText: Boolean) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = s?.toString()?.toIntOrNull() ?: 0

                if (isHourEditText) {
                    timeHourText =  when {
                        value == 24 -> 0
                        else -> value
                    }
                    updateTextField(timeHourEditText, timeHourText, 25, errorMessage)
                } else {
                    timeMinText = value
                    updateTextField(timeMinEditText, timeMinText, 60, errorMessage)
                }

                Log.d("customTag", "timeHourText updated: $timeHourText | timeMinText updated: $timeMinText")
                calendar.set(Calendar.HOUR_OF_DAY, timeHourText)
                calendar.set(Calendar.MINUTE, timeMinText)
                calendar.set(Calendar.SECOND, 0)
            }

            override fun afterTextChanged(s: Editable?) {}
        }


        timeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                timeLayout.visibility = View.VISIBLE
                time = 1

                timeHourText = 0
                timeMinText = 0

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)

                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch checked")

                //시간 설정------------------------------------------------------------------------------------------

                timeHourEditText.addTextChangedListener(createTextWatcher(isHourEditText = true))
                timeMinEditText.addTextChangedListener(createTextWatcher(isHourEditText = false))


                //알림 설정------------------------------------------------------------------------------------------
                notifyToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        noti = when (checkedId) {
                            R.id.notiDefault -> 0
                            R.id.notiDay -> 1
                            R.id.notiHour -> 2
                            R.id.notiMin -> 3
                            else -> noti // 기본값 유지
                        }
                        Log.d("customTag", "notify value updated: $noti")
                    }
                }

                // 알림 설정 info 리스너
                val infoButton2 = dialogView.findViewById<Button>(R.id.infoButton2)
                infoButton2.setOnClickListener {
                    Toast.makeText(context, R.string.auto_info2, Toast.LENGTH_SHORT).show()
                    Log.d("customTag", "ScheduleFragment onViewCreated called; infoButton2 clicked")
                }
            }
            else {
                timeLayout.visibility = View.GONE
                timeHourText = 0
                timeMinText = 0
                noti = 0
                auto = 0
                time = 0
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch unchecked")
            }

        }

        //메모
        val memo = dialogView.findViewById<EditText>(R.id.memoText)
        memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memoText = s?.toString() ?: ""
                Log.d("customTag", "ScheduleFragment onViewCreated called; memoText: $memoText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 다이얼로그가 닫힐 때
        dialog.setOnDismissListener {
            clickCheck = false
        }

        // 다이얼로그 표시
        dialog.show()
    }


    //정적 멤버
    companion object {
        fun newInstance(): ScheduleFragment {
            val args = Bundle()
            val fragment = ScheduleFragment()
            fragment.arguments = args
            return fragment
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val click: LinearLayout = view.findViewById(R.id.clickLayout)
        val scheduleRecyclerView: RecyclerView = view.findViewById(R.id.scheduleRecyclerView)
    }
}