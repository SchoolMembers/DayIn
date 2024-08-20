package com.schedule.dayin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
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
import com.schedule.dayin.data.mainD.TimeData
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

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var currentDayViewContainer: DayViewContainer? = null

    var loadContainer: DayViewContainer? = null

    //데이터 체크
    private var clickCheck = false

    private lateinit var adapter: ScheduleAdapter

    private var dataList = mutableListOf<TimeData>()




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


    //몇 번째 주?
    private var weekOfMonth = 0
    //셀 높이 조정
    private var cellHeight = 300
    private var dataSize = 0


    //database
    private lateinit var mainDb: MainDatabase
    private lateinit var scheduleRepository: ScheduleRepository

    private var calendar = Calendar.getInstance()

    //시간 등록
    private var timeAmPmIndex = 0
    private var time = 0

    //알림 설정 여부
    private var noti: Int = 0

    //메모
    private var memoText: String? = ""

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
        val calendarView = binding.calendarView
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(100)
        val lastMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        /*val firstDayOfWeekNumber = firstDayOfWeek.value 일요일은 1, 토요일은 7
        임시 저장*/


        //시작 월, 종료 월, 첫 주의 요일
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.SeventhWidth

        Log.d("customTag", "ScheduleFragment onViewCreated called; day setup complete")


        //database setting
        val appController = requireActivity().application as AppController
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

                weekOfMonth = 0
                cellHeight = 300
                dataSize = 0
            }
        }

    }

    //리사이클러 비동기 데이터 로그 함수
    fun dataLoad(container: DayViewContainer, day: CalendarDay) {
        uiScope.launch {
            val dataList = loadScheduleDataForDay(day)

            // 몇 번째 주인지 구하기
            val weekFields = WeekFields.of(Locale.getDefault())
            val todayWeek = day.date.get(weekFields.weekOfMonth())

            if (todayWeek != weekOfMonth) {
                weekOfMonth = todayWeek
                cellHeight = 300
                dataSize = 0
            }

            Log.d("ScheduleData", "Date: ${day.date}, Loaded Data: $dataList")
            if (dataList.isNotEmpty()) {
                if (container.scheduleRecyclerView.adapter == null) {
                    adapter = ScheduleAdapter(requireContext(), dataList, clickCheck)
                    container.scheduleRecyclerView.adapter = adapter
                    container.scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
                } else {
                    (container.scheduleRecyclerView.adapter as ScheduleAdapter).updateData(dataList)
                }
            } else {
                container.scheduleRecyclerView.adapter = null
            }

            if (dataList.size > dataSize && todayWeek == weekOfMonth) {
                dataSize = dataList.size
                if (dataList.size > 3) {
                    cellHeight = (dataSize + 1) * 75
                }

            }

            withContext(Dispatchers.Main) {
                container.setHeight(cellHeight)
            }

        }
    }

    //리사이클러 데이터 세팅
    suspend fun loadScheduleDataForDay(day: CalendarDay): MutableList<TimeData> {
        val date = day.date
        val dataList = mutableListOf<TimeData>()

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
                    dataList.add(TimeData(schedule.id, schedule.date, schedule.title, schedule.time, 0))
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
                val adapterD = ScheduleAdapter(requireContext(), dataList, clickCheck)
                recyclerViewInDialog.adapter = adapterD
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
        var titleText: String = ""

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                titleText = s?.toString() ?: ""
                Log.d("customTag", "ScheduleFragment onViewCreated called; titleText: $titleText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
                            color = 0
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
        val timeAmPm = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.timeAmPm)


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
        fun createTextWatcher(isPm: Boolean, isHourEditText: Boolean) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = s?.toString()?.toIntOrNull() ?: 0

                if (isHourEditText) {
                    timeHourText = when {
                        isPm && value in 1..11 -> value + 12
                        isPm && value == 12 -> 12
                        !isPm && value == 12 -> 0
                        !isPm && value == 0 -> 0
                        else -> value
                    }
                    updateTextField(timeHourEditText, timeHourText, 24, errorMessage)
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
                timeAmPmIndex = 0 //0은 am, 1은 pm
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch checked")

                //시간 설정------------------------------------------------------------------------------------------

                if (timeAmPmIndex == 0) {
                    timeHourEditText.addTextChangedListener(createTextWatcher(isPm = false, isHourEditText = true))
                    timeMinEditText.addTextChangedListener(createTextWatcher(isPm = false, isHourEditText = false))
                } else {
                    timeHourEditText.addTextChangedListener(createTextWatcher(isPm = true, isHourEditText = true))
                    timeMinEditText.addTextChangedListener(createTextWatcher(isPm = true, isHourEditText = false))
                }

                timeAmPm.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        when (checkedId) {
                            R.id.timeAm -> {
                                Log.d("customTag", "ScheduleFragment onViewCreated called; timeAm clicked")
                                timeAmPmIndex = 0
                                if (timeHourText == 12) {
                                    timeHourText = 0
                                }
                                timeHourEditText.addTextChangedListener(createTextWatcher(isPm = false, isHourEditText = true))
                                timeMinEditText.addTextChangedListener(createTextWatcher(isPm = false, isHourEditText = false))
                            }
                            R.id.timePm -> {
                                Log.d("customTag", "ScheduleFragment onViewCreated called; timePm clicked")
                                timeAmPmIndex = 1
                                if (timeHourText in 1..11) {
                                    timeHourText += 12
                                }
                                timeHourEditText.addTextChangedListener(createTextWatcher(isPm = true, isHourEditText = true))
                                timeMinEditText.addTextChangedListener(createTextWatcher(isPm = true, isHourEditText = false))
                            }
                        }

                        //editText 빈 값 처리
                        when (timeAmPmIndex) {
                            0 -> {
                                if (timeHourEditText.text.toString().trim().isEmpty()) {
                                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                                } else {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeHourText)
                                }
                            }
                            1 -> {
                                if (timeHourEditText.text.toString().trim().isEmpty()) {
                                    calendar.set(Calendar.HOUR_OF_DAY, 12)
                                } else {
                                    calendar.set(Calendar.HOUR_OF_DAY, timeHourText)
                                }
                            }
                        }

                        if (timeMinEditText.text.toString().trim().isEmpty()) {
                            calendar.set(Calendar.MINUTE, 0)
                        }
                    }
                }

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

        //셀 크기 조절
        fun setHeight(cellHeight: Int) {

            val params = view.layoutParams
            params.height = cellHeight
            view.layoutParams = params

        }
    }
}