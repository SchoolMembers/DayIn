package com.schedule.dayin.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*


class ScheduleFragment : Fragment() {

    private var _binding: FragmentSBinding? = null
    private val binding get() = _binding!!

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
        calendarView.daySize = DaySize.Rectangle

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
                container.textView.text = day.date.dayOfMonth.toString() //dayText에 날짜 표시
                if (day.position == DayPosition.MonthDate) { //현재 날짜가 현재 월 내에 있을 때
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }
                // 날짜 아이템 클릭 리스너 설정
                container.view.setOnClickListener {
                    // 날짜 클릭 시 다이얼로그 표시
                    showDateDialog(day)
                    Log.d("customTag", "ScheduleFragment onViewCreated called; day clicked")
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

    }

    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    //날짜 클릭 다이얼로그 메서드
    private fun showDateDialog(day: CalendarDay) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_s, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // 다이얼로그 내의 뷰들을 참조해 날짜 정보 설정
        val monYearTextView = dialogView.findViewById<TextView>(R.id.monYear)
        monYearTextView.text = day.date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)").withLocale(Locale.KOREAN))
        Log.d("customTag", "ScheduleFragment onViewCreated called; monYearTextView updated")

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; dialog closed")
        }

        // 추가 버튼에 대한 클릭 리스너
        val addButton = dialogView.findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            showAddDialog(day)
        }

        // 다이얼로그 표시
        dialog.show()
    }

    private fun showAddDialog(day: CalendarDay) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_s_add, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val dialog = dialogBuilder.create()


        // 다이얼로그 내의 뷰들을 참조해 날짜 정보 설정
        val monYearTextView = dialogView.findViewById<TextView>(R.id.monYear)
        monYearTextView.text = day.date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)").withLocale(Locale.KOREAN))
        Log.d("customTag", "ScheduleFragment onViewCreated called; monYearTextView updated")

        //date 설정
        calendar = Calendar.getInstance()
        calendar.set(day.date.year, day.date.monthValue - 1, day.date.dayOfMonth) // 날짜 설정

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
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


        // 체크 버튼
        val checkButton = dialogView.findViewById<Button>(R.id.checkButton)
        checkButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; check button clicked")
            val scheduleDate = calendar.time

            //데이터 저장(일정 추가)
            CoroutineScope(Dispatchers.IO).launch {

                if (memoText == ""){
                    memoText = null
                }

                scheduleRepository.insertSche(
                    ScheduleDb(date = scheduleDate, title = titleText, auto = auto, notify = noti, memo = memoText, check = 0, time = time)
                )

                // UI 업데이트는 메인 스레드에서 수행
                withContext(Dispatchers.Main) {
                    // collect를 메인 스레드에서 수행
                    CoroutineScope(Dispatchers.Main).launch {
                        scheduleRepository.allSchedules().collect { scheList ->
                            // 로그를 통해 결과를 확인
                            scheList.forEach { scheduleDb ->
                                Log.d("customTag", scheduleDb.toString())
                            }
                        }
                    }
                }
            }
            Log.d("customTag", "ScheduleFragment onViewCreated called; data saved")
        }

        //시간 등록 활성화 버튼
        val timeSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.timeSwitch)
        val timeLayout = dialogView.findViewById<View>(R.id.timeLayout)
        val timeAmPm = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.timeAmPm)
        val timeHourEditText = dialogView.findViewById<EditText>(R.id.timeHour)
        val timeMinEditText = dialogView.findViewById<EditText>(R.id.timeMin)

        var textWatcher: TextWatcher? = null

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
                val value = s.toString().toIntOrNull() ?: 0

                if (isHourEditText) {
                    timeHourText = if (isPm) {
                        if (value < 12 && value != 0) value + 12 else value
                    } else {
                        if (value == 12) 0 else value
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
                            }
                            R.id.timePm -> {
                                Log.d("customTag", "ScheduleFragment onViewCreated called; timePm clicked")
                                timeAmPmIndex = 1
                            }
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
    }
}