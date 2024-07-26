package com.example.dayin.adapter

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dayin.fragments.CalendarFragment
import java.time.LocalDate

//SMD Fragment에서 사용
class CalendarPagerAdapter(activity: FragmentActivity, private val startDate: LocalDate) : FragmentStateAdapter(activity) {

    //시작 페이지 Int타입 Max / 2
    val startingPosition = Int.MAX_VALUE / 2

    //페이지 수 get 함수
    override fun getItemCount(): Int {
        Log.d("CalendarPagerAdapter", "getItemCount called")
        return Int.MAX_VALUE
    }

    //페이지 생성 함수
    override fun createFragment(position: Int): CalendarFragment {
        val offset = position - startingPosition
        val date = startDate.plusMonths(offset.toLong())
        return CalendarFragment.newInstance(date)
    }
}