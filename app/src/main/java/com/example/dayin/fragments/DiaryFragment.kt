package com.example.dayin.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dayin.MainActivity
import com.example.dayin.R
import com.example.dayin.databinding.FragmentDBinding
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

class DiaryFragment : Fragment() {

    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!


    //프래그먼트 뷰를 생성하고 초기화. 프래그먼트의 레이아웃 인플레이트 -> 뷰 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        Log.d("costumTag", "DiaryFragment onCreateView called")
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


        //시작 월, 종료 월, 첫 주의 요일
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.Rectangle

        Log.d("customTag", "DiaryFragment onViewCreated called; day setup complete")


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

            }
        }

        //스크롤 리스너 ( 월 스크롤 -> 연, 월 업데이트)
        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                barDateYear?.text = p1.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
                Log.d("customTag", "DiaryFragment onViewCreated called; barDateYear updated")
            }
        }

    }

    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    //정적 멤버
    companion object {
        fun newInstance(): DiaryFragment {
            val args = Bundle()
            val fragment = DiaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
    }
}