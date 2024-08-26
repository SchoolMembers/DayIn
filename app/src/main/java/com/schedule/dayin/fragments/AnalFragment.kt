package com.schedule.dayin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.MainActivity
import com.schedule.dayin.R
import java.time.LocalDate
import java.time.YearMonth

class AnalFragment: Fragment() {

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
        val view = inflater.inflate(R.layout.analysis_viewpager, container, false)
        return view
    }


    //정적 멤버
    companion object {
        private const val ARG_DATE = "date"

        //프래그먼트 생성자
        @JvmStatic
        fun newInstance(date: LocalDate) =
            AnalFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }


}