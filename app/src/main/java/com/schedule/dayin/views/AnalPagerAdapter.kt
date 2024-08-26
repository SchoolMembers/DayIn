package com.schedule.dayin.views

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.schedule.dayin.AnalysisActivity
import com.schedule.dayin.fragments.AnalFragment
import java.time.LocalDate

class AnalPagerAdapter(activity: AnalysisActivity, private val startDate: LocalDate): FragmentStateAdapter(activity) {

    //시작 페이지 Int타입 Max / 2
    val startingPosition = 150

    //페이지 수 get 함수
    override fun getItemCount(): Int {
        return 300
    }

    //페이지 생성 함수
    override fun createFragment(position: Int): AnalFragment {
        val offset = position - startingPosition
        val date = startDate.plusMonths(offset.toLong())
        return AnalFragment.newInstance(date)
    }
}