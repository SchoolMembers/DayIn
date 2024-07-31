package com.example.dayin.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.dayin.MainActivity
import com.example.dayin.R
import com.example.dayin.adapter.CalendarPagerAdapter
import com.example.dayin.databinding.FragmentDBinding
import com.example.dayin.databinding.FragmentSBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

//MainActivity에서 사용
class DiaryFragment: Fragment() {

    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!
    private var dayCount = 4
    private lateinit var today: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        today = (activity as? MainActivity)?.fetchToday() ?: LocalDate.now()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(binding.calendarView) {
            itemAnimator = null
        }

        val barDateYear = (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)
        barDateYear?.text = monthYearFromDate(today)

        val calendarView = binding.calendarView
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(100)
        val lastMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.Rectangle




        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                if (day.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }

            }
        }

        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                barDateYear?.text = p1.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        return date.format(formatter)
    }


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