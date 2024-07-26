package com.example.dayin.fragments

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//MainActivity에서 사용
class DiaryFragment: Fragment() {

    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CalendarPagerAdapter
    private lateinit var today: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        today = (activity as? MainActivity)?.fetchToday() ?: LocalDate.now()
        adapter = CalendarPagerAdapter(requireActivity(), today)
        Log.d("DiaryFragment", "today: $today, adapter: $adapter")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        Log.d("DiaryFragment", "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)?.text = monthYearFromDate(today)

        binding.viewPagerDay.adapter = adapter
        binding.viewPagerDay.setCurrentItem(adapter.startingPosition, true)

        binding.viewPagerDay.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val offset = position - adapter.startingPosition
                val selectedDate = today.plusMonths(offset.toLong())
                (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)?.text = monthYearFromDate(selectedDate)
                Log.d("DiaryFragment", "activity_main.xml id: barDateYear text changed to $selectedDate")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        Log.d("DiaryFragment", "monthYearFromDate called")
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
}