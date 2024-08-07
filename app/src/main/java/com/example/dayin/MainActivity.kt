package com.example.dayin

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.dayin.data.mainD.MainDatabase
import com.example.dayin.data.mainD.MoneyDb
import com.example.dayin.data.mainD.ScheduleDb
import com.example.dayin.data.mainD.repository.CateRepository
import com.example.dayin.data.mainD.repository.DiaryRepository
import com.example.dayin.data.mainD.repository.MoneyRepository
import com.example.dayin.data.mainD.repository.ScheduleRepository
import com.example.dayin.data.memoD.MemoDatabase
import com.example.dayin.data.memoD.repository.MemoRepository
import com.example.dayin.databinding.ActivityMainBinding
import com.example.dayin.fragments.DiaryFragment
import com.example.dayin.fragments.MoneyFragment
import com.example.dayin.fragments.ScheduleFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var today: LocalDate

    //데이터 베이스
    private lateinit var mainDb: MainDatabase
    private lateinit var memoDb: MemoDatabase

    private lateinit var scheduleRepository: ScheduleRepository


    //smd 버튼 식별자 CalendarFragment 전달
    private var smdButton = 0


    //오늘 날짜 전달 함수
    /*fun fetchToday(): LocalDate {
        return today
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // activity_main.xml binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //database setting
        mainDb = MainDatabase.getDatabase(this)

        memoDb = MemoDatabase.getDatabase(this)

        scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())

        //layout setting
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        today = LocalDate.now()

        Log.d("customTag", "MainActivity onCreate called; today: $today")


        //ScheduleFragment setting
        if (savedInstanceState == null) {
            val defaultFragment = ScheduleFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, defaultFragment)
                .commitNow()
        }
        Log.d("customTag", "MainActivity onCreate called; defaultFragment setting (activity_main.xml -> fragmentSMD)")

        //smd button click event
        binding.smdS.setOnClickListener {
            smdButton = 0
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, ScheduleFragment.newInstance())
                .commit()
            Log.d("customTag", "MainActivity onCreate called; click smd button S (smdButton = 0) set fragment to ScheduleFragment")

            val calendar = Calendar.getInstance()
            calendar.set(2024, Calendar.AUGUST, 4, 14, 30) // 날짜와 시간 설정
            val scheduleDate = calendar.time

            CoroutineScope(Dispatchers.IO).launch {

                scheduleRepository.insertSche(ScheduleDb(date = scheduleDate, auto = 0, notify = 0, memo = "test", check = 0, time = Date(System.currentTimeMillis()), loc = "test"))

                // UI 업데이트는 메인 스레드에서 수행
                withContext(Dispatchers.Main) {
                    // collect를 메인 스레드에서 수행
                    CoroutineScope(Dispatchers.IO).launch {
                        scheduleRepository.allSchedules().collect { scheList ->
                            // 로그를 통해 결과를 확인
                            scheList.forEach { ScheduleDb ->
                                Log.d("customTag", ScheduleDb.toString())
                            }
                        }
                    }
                }
            }
        }

        binding.smdM.setOnClickListener {
            smdButton = 1
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, MoneyFragment.newInstance())
                .commit()
            Log.d("customTag", "MainActivity onCreate called; click smd button M (smdButton = 1) set fragment to ScheduleFragment")
        }

        binding.smdD.setOnClickListener {
            smdButton = 2
            updateButtonStyles()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSMD, DiaryFragment.newInstance())
                .commit()
            Log.d("customTag", "MainActivity onCreate called; click smd button D (smdButton = 2) set fragment to ScheduleFragment")
        }
    }

    //smd 버튼 스타일 변경 함수
    private fun updateButtonStyles() {
        when (smdButton) {
            //smd S
            0 -> {
                binding.smdS.setBackgroundResource(R.drawable.touch_smd_left) //button background
                binding.smdM.setBackgroundResource(R.drawable.smd_background)
                binding.smdD.setBackgroundResource(R.drawable.smd_right)
                binding.smdS.setTextColor(resources.getColor(R.color.lightGray)) //text color
                binding.smdM.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdD.setTextColor(resources.getColor(R.color.darkGray))
            }
            //smd M
            1 -> {
                binding.smdS.setBackgroundResource(R.drawable.smd_left)
                binding.smdM.setBackgroundResource(R.drawable.touch_smd_background)
                binding.smdD.setBackgroundResource(R.drawable.smd_right)
                binding.smdS.setTextColor(resources.getColor(R.color.darkGray))
                binding.smdM.setTextColor(resources.getColor(R.color.lightGray))
                binding.smdD.setTextColor(resources.getColor(R.color.darkGray))
            }
            //smd D
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


