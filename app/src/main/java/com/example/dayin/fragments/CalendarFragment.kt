package com.example.dayin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayin.R
import com.example.dayin.adapter.CalendarAdapter
import java.time.LocalDate
import java.time.YearMonth
import androidx.fragment.app.Fragment
import com.example.dayin.MainActivity

//CalendarPagerAdapter에서 사용
class CalendarFragment : Fragment() {

    private lateinit var date: LocalDate

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
        //smdButton 식별자 값에 따른 layout 결정
        var layoutType = 0
        when((activity as? MainActivity)?.smdButton) {
            0 -> {
                layoutType = 0
            }
            1 -> {
                layoutType = 1
            }
            2 -> {
                layoutType = 2
            }
            else -> Log.d("CalendarFragment","fragment error")
        }
        Log.d("CalendarFragment", "layoutType: $layoutType")

        val view = inflater.inflate(R.layout.fragment_calendar, container, false) //fragment_calendar.xml inflate
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView) //fragment_calendar.xml에서 recyclerView id
        recyclerView.layoutManager = GridLayoutManager(context, 7) //7칸씩 배치
        recyclerView.adapter = CalendarAdapter(dayInMonthArray(date), layoutType) //recyclerView adapter에 CalendarAdapter 설정
        return view
    }

    //날짜 배열 생성 함수
    private fun dayInMonthArray(date: LocalDate): ArrayList<String> {
        val dayList = ArrayList<String>()
        val yearMonth = YearMonth.from(date) //연도, 월
        val lastDay = yearMonth.lengthOfMonth() //달의 마지막 날짜
        val firstDay = date.withDayOfMonth(1) //달의 첫 날짜
        val dayOfWeek = firstDay.dayOfWeek.value % 7 //첫 날의 요일 (일요일이 0)

        //날짜 배열 생성
        for (i in 0 until  42) {
            if (i < dayOfWeek || i >= (lastDay + dayOfWeek)) {
                dayList.add("")
            } else {
                dayList.add((i - dayOfWeek + 1).toString())
            }
        }
        return dayList
        Log.d("CalendarFragment", "dayList create complete")
    }

    //정적 멤버
    companion object {
        private const val ARG_DATE = "date"

        //프래그먼트 생성자
        @JvmStatic
        fun newInstance(date: LocalDate) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }
}