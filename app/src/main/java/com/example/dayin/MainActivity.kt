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
import com.example.dayin.fragments.DiaryFragment
import com.example.dayin.fragments.MoneyFragment
import com.example.dayin.fragments.ScheduleFragment
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var today: LocalDate

    var smdButton = 0

    fun fetchToday(): LocalDate {
        return today
    }

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

        today = LocalDate.now()

        Log.d("MainActivity", "today: $today")

        if (savedInstanceState == null) {
            val defaultFragment = ScheduleFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, defaultFragment)
                .commitNow()
        }

        Log.d("MainActivity", "defaultFragment setting")

        binding.smdS.setOnClickListener {
            smdButton = 0
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, ScheduleFragment.newInstance())
                .commit()
        }

        binding.smdM.setOnClickListener {
            smdButton = 1
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, MoneyFragment.newInstance())
                .commit()
        }

        binding.smdD.setOnClickListener {
            smdButton = 2
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, DiaryFragment.newInstance())
                .commit()
        }
    }

    private fun updateButtonStyles() {
        when (smdButton) {
            0 -> {
                binding.smdS.setBackgroundResource(R.drawable.touch_smd_left)
                binding.smdM.setBackgroundResource(R.drawable.smd_background)
                binding.smdD.setBackgroundResource(R.drawable.smd_right)
                binding.smdS.setTextColor(resources.getColor(R.color.lightGray))
                binding.smdM.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdD.setTextColor(resources.getColor(R.color.darkGray))
            }
            1 -> {
                binding.smdS.setBackgroundResource(R.drawable.smd_left)
                binding.smdM.setBackgroundResource(R.drawable.touch_smd_background)
                binding.smdD.setBackgroundResource(R.drawable.smd_right)
                binding.smdS.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdM.setTextColor(resources.getColor(R.color.lightGray))
                binding.smdD.setTextColor(resources.getColor(R.color.darkGray))
            }
            2 -> {
                binding.smdS.setBackgroundResource(R.drawable.smd_left)
                binding.smdM.setBackgroundResource(R.drawable.smd_background)
                binding.smdD.setBackgroundResource(R.drawable.touch_smd_right)
                binding.smdS.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdM.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdD.setTextColor(resources.getColor(R.color.lightGray))
            }
        }
    }
}


