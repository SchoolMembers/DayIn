package com.example.dayin.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dayin.AppController
import com.example.dayin.MainActivity
import com.example.dayin.R
import com.example.dayin.data.mainD.MainDatabase
import com.example.dayin.data.mainD.repository.ScheduleRepository
import com.example.dayin.databinding.FragmentSBinding
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

class ScheduleFragment : Fragment() {

    private var _binding: FragmentSBinding? = null
    private val binding get() = _binding!!

    //database
    private lateinit var mainDb: MainDatabase
    private lateinit var scheduleRepository: ScheduleRepository


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

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; dialog closed")
        }

        // 체크 버튼
        val checkButton = dialogView.findViewById<Button>(R.id.checkButton)
        checkButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; check button clicked")
        }

        //시간 등록 활성화 버튼
        val timeSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.timeSwitch)
        val locSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.locSwitch)
        val timeLayout = dialogView.findViewById<View>(R.id.timeLayout)
        val locLayout = dialogView.findViewById<View>(R.id.locLayout)

        //시간 등록 활성화 버튼 클릭 리스너
        timeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                timeLayout.visibility = View.VISIBLE
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch checked")
            } else {
                timeLayout.visibility = View.GONE
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch unchecked")
            }
        }

        //장소 등록 활성화 버튼 클릭 리스너
        locSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                locLayout.visibility = View.VISIBLE
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch checked")
            } else {
                locLayout.visibility = View.GONE
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch unchecked")
            }
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
    }
}