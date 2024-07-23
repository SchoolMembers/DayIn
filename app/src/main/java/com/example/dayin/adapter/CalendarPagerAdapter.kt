package com.example.dayin.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dayin.fragments.CalendarFragment
import java.time.LocalDate

class CalendarPagerAdapter(activity: FragmentActivity, private val startDate: LocalDate) : FragmentStateAdapter(activity) {

    val startingPosition = Int.MAX_VALUE / 2

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun createFragment(position: Int): CalendarFragment {
        val offset = position - startingPosition
        val date = startDate.plusMonths(offset.toLong())
        return CalendarFragment.newInstance(date)
    }
}