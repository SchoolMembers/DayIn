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

        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 7)
        recyclerView.adapter = CalendarAdapter(dayInMonthArray(date), layoutType)
        return view
    }

    private fun dayInMonthArray(date: LocalDate): ArrayList<String> {
        val dayList = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val lastDay = yearMonth.lengthOfMonth()
        val firstDay = date.withDayOfMonth(1)
        val dayOfWeek = firstDay.dayOfWeek.value % 7

        for (i in 0 until  42) {
            if (i < dayOfWeek || i >= (lastDay + dayOfWeek)) {
                dayList.add("")
            } else {
                dayList.add((i - dayOfWeek + 1).toString())
            }
        }
        return dayList
    }

    companion object {
        private const val ARG_DATE = "date"

        @JvmStatic
        fun newInstance(date: LocalDate) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }
}