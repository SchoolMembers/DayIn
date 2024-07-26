package com.example.dayin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.dayin.R
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.dayin.MainActivity
import com.example.dayin.adapter.CalendarPagerAdapter
import com.example.dayin.databinding.FragmentSBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//MainActivity에서 사용
class ScheduleFragment : Fragment() {

    //fragment_s.xml 바인딩
    private var _binding: FragmentSBinding? = null
    private val binding get() = _binding!!

    //CalendarPagerAdapter 설정 (viewPager2)
    private lateinit var adapter: CalendarPagerAdapter
    //오늘 날짜 받아올 객체
    private lateinit var today: LocalDate


    //프래그먼트가 생성될 때 호출. 초기 설정 수행
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        today = (activity as? MainActivity)?.fetchToday() ?: LocalDate.now()
        adapter = CalendarPagerAdapter(requireActivity(), today)
        Log.d("ScheduleFragment", "today: $today, adapter: $adapter")
    }

    //프래그먼트 뷰를 생성하고 초기화. 프래그먼트의 레이아웃 인플레이트 -> 뷰 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSBinding.inflate(inflater, container, false)
        Log.d("ScheduleFragment", "onCreateView called")
        return binding.root
    }

    //onCreateView() 완료 후 호출. 프래그먼트 뷰 생성 이후 추가적인 초기화 작업. (뷰 관련 로직 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)?.text = monthYearFromDate(today)

        //CalendarPagerAdapter
        binding.viewPagerDay.adapter = adapter
        binding.viewPagerDay.setCurrentItem(adapter.startingPosition, true)

        //페이지 전환 함수
        binding.viewPagerDay.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val offset = position - adapter.startingPosition
                val selectedDate = today.plusMonths(offset.toLong())
                (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)?.text = monthYearFromDate(selectedDate)
                Log.d("ScheduleFragment", "activity_main.xml id: barDateYear text changed to $selectedDate")
            }
        })
    }

    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    //날짜를 "yyyy년 MM월" 형식으로 변환하는 함수
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        Log.d("ScheduleFragment", "monthYearFromDate called")
        return date.format(formatter)
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
}

