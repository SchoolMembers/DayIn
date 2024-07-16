package com.example.dayin

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.dayin.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var today: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //현재 날짜
        today = LocalDate.now()

        //화면 설정
        setMonthView()

        //이전달 버튼
        binding.preBtn.setonClickListener {
            today = today.minusMonths(1)
            setMonthView()
        }

        //다음달 버튼
        binding.nextBtn.setOnClickListener {
            today = today.plusMonths(1)
            setMonthView()
        }

    }

    //날짜 화면에 보여주기
    private fun setMonthView() {
        //연월 텍스트뷰 세팅
        binding.barDateYear.text = monthYearFromDate(today)

        //날짜 생성해서 리스트에 담기
        val dayList = dayInMonthArray(today)

        //어댑터 초기화
        val adapter = CalendarAdapter(dayList)

        //layout setting(열 7개)
        var manager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 7)

        //layout 적용
        binding.viewPagerDay.layoutManager = manager

        //어댑터 적용
        binding.viewPagerDay.adapter = adapter
    }

    //날짜 포매팅 설정
    private fun monthYearFromDate(date: LocalDate): String {
        var formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")

        return date.format(formatter)
    }

    //날짜 생성
    private fun dayInMonthArray(date: LocalDate): ArrayList<String>{
        var dayList = ArrayList<String>()

        var yearMonth = YearMonth.from(date)

        //해당 월 마지막 날짜 가져오기
        var lastDay = yearMonth.lengthOfMonth()

        //해당 월 첫 번째 날짜
        var firstDay = today.withDayOfMonth(1)

        //첫 번째 날 요일
        var dayOfWeek = firstDay.dayOfWeek.value

        for(i in 1..41){
            if(i <= dayOfWeek || i > (lastDay + dayOfWeek)){
                dayList.add("")
            } else{
                dayList.add((i - dayOfWeek).toString())
            }
        }
        return dayList
    }
}