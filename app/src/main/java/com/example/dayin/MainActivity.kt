package com.example.dayin

import android.os.Bundle
import android.util.Log
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
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var today: LocalDate
    private lateinit var adapter: CalendarPagerAdapter

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

        // 현재 날짜
        today = LocalDate.now()

        // 어댑터 초기화
        adapter = CalendarPagerAdapter(this, today)

        // ViewPager2 설정
        binding.viewPagerDay.adapter = adapter
        binding.viewPagerDay.setCurrentItem(adapter.startingPosition, true)

        // 초기 날짜 설정
        binding.barDateYear.text = monthYearFromDate(today)

        //SMD 바 버튼 설정
        binding.smdS.setOnClickListener {
            binding.smdS.setBackgroundResource(R.drawable.touch_smd_left)
            binding.smdM.setBackgroundResource(R.drawable.smd_background)
            binding.smdD.setBackgroundResource(R.drawable.smd_right)

            binding.smdS.setTextColor(resources.getColor(R.color.lightGray))
            binding.smdM.setTextColor(resources.getColor(R.color.darkGray))
            binding.smdD.setTextColor(resources.getColor(R.color.darkGray))

            Log.d("MainActivity", "clicked smdS")

        }
        binding.smdM.setOnClickListener {
            binding.smdS.setBackgroundResource(R.drawable.smd_left)
            binding.smdM.setBackgroundResource(R.drawable.touch_smd_background)
            binding.smdD.setBackgroundResource(R.drawable.smd_right)

            binding.smdS.setTextColor(resources.getColor(R.color.darkGray))
            binding.smdM.setTextColor(resources.getColor(R.color.lightGray))
            binding.smdD.setTextColor(resources.getColor(R.color.darkGray))

            Log.d("MainActivity", "clicked smdM")
        }
        binding.smdD.setOnClickListener {
            binding.smdS.setBackgroundResource(R.drawable.smd_left)
            binding.smdM.setBackgroundResource(R.drawable.smd_background)
            binding.smdD.setBackgroundResource(R.drawable.touch_smd_right)

            binding.smdS.setTextColor(resources.getColor(R.color.darkGray))
            binding.smdM.setTextColor(resources.getColor(R.color.darkGray))
            binding.smdD.setTextColor(resources.getColor(R.color.lightGray))

            Log.d("MainActivity", "clicked smdD")
        }

        // ViewPager2 페이지 변경 리스너
        binding.viewPagerDay.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val offset = position - adapter.startingPosition
                val selectedDate = today.plusMonths(offset.toLong())
                binding.barDateYear.text = monthYearFromDate(selectedDate)

                Log.d("MainActivity", "Calendar selected: $selectedDate, position: $position")
            }
        })
    }

    // 날짜 포매팅 설정
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월")
        return date.format(formatter)
    }
}
