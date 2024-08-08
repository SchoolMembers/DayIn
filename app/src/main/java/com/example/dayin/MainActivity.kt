package com.example.dayin

import android.icu.util.Calendar
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dayin.data.mainD.MainDatabase
import com.example.dayin.data.mainD.ScheduleDb
import com.example.dayin.data.mainD.repository.ScheduleRepository
import com.example.dayin.data.memoD.MemoDatabase
import com.example.dayin.databinding.ActivityMainBinding
import com.example.dayin.fragments.DiaryFragment
import com.example.dayin.fragments.MoneyFragment
import com.example.dayin.fragments.ScheduleFragment
import com.google.android.material.tabs.TabLayout
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
    /*private lateinit var mainDb: MainDatabase
    private lateinit var memoDb: MemoDatabase

    private lateinit var scheduleRepository: ScheduleRepository*/




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

        //하단 바 활성화 상태
        binding.bottomNavigation.selectedItemId = R.id.barHome

        //smd 탭 버튼
        setupTabSelectedListener()

        //database setting
        /*val appController = application as AppController
        mainDb = appController.mainDb
        memoDb = appController.memoDb

        scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())*/

        // 상단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainTopBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // 하단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
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


        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //bottom navigation click event
        val memoIntent = Intent(this, MemoActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.barHome -> {
                    startActivity(homeIntent, options.toBundle())
                    Log.d("customTag", "MainActivity onCreate called; click home button")
                    true
                }
                R.id.barMemo -> {
                    startActivity(memoIntent, options.toBundle())
                    Log.d("customTag", "MainActivity onCreate called; click memo button")
                    true
                }
                else -> false
            }
        }
    }


    //tab layout 설정
    private fun setupTabSelectedListener() {
        binding.blankSMD.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentSMD, ScheduleFragment.newInstance())
                            .commit()
                        Log.d("customTag", "MainActivity onCreate called; set fragment to ScheduleFragment")

                        /*val calendar = Calendar.getInstance()
                        calendar.set(2024, Calendar.AUGUST, 4, 14, 30) // 날짜와 시간 설정
                        val scheduleDate = calendar.time

                        CoroutineScope(Dispatchers.IO).launch {

                            scheduleRepository.insertSche(
                                ScheduleDb(date = scheduleDate, auto = 0, notify = 0, memo = "test", check = 0, time = Date(System.currentTimeMillis()), loc = "test")
                            )

                            // UI 업데이트는 메인 스레드에서 수행
                            withContext(Dispatchers.Main) {
                                // collect를 메인 스레드에서 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    scheduleRepository.allSchedules().collect { scheList ->
                                        // 로그를 통해 결과를 확인
                                        scheList.forEach { scheduleDb ->
                                            Log.d("customTag", scheduleDb.toString())
                                        }
                                    }
                                }
                            }
                        }*/
                    }

                    1 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentSMD, MoneyFragment.newInstance())
                            .commit()
                        Log.d("customTag", "MainActivity onCreate called; set fragment to ScheduleFragment")
                    }

                    2 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentSMD, DiaryFragment.newInstance())
                            .commit()
                        Log.d("customTag", "MainActivity onCreate called; set fragment to ScheduleFragment")
                    }

                    else -> Log.d("customTag", "MainActivity onCreate called; tab layout error")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Optional: Handle tab unselected if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Handle tab reselected if needed
            }
        })
    }

}


