package com.schedule.dayin

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.schedule.dayin.databinding.SettingActivityBinding

class SettingActivity: AppCompatActivity() {

    private lateinit var binding: SettingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        //하단 바 활성화 상태
        binding.bottomNavigation.selectedItemId = R.id.barMenu

        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //bottom navigation click event
        val memoIntent = Intent(this, MemoActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)
        val analysisIntent = Intent(this, AnalysisActivity::class.java)
        val menuIntent = Intent(this, SettingActivity::class.java)

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.barHome -> {
                    startActivity(homeIntent, options.toBundle())
                    Log.d("customTag", "SettingActivity onCreate called; click home button")
                    true
                }
                R.id.barMemo -> {
                    startActivity(memoIntent, options.toBundle())
                    Log.d("customTag", "SettingActivity onCreate called; click memo button")
                    true
                }
                R.id.barMoney -> {
                    startActivity(analysisIntent, options.toBundle())
                    Log.d("customTag", "SettingActivity onCreate called; click analysis button")
                    true
                }
                R.id.barMenu -> {
                    startActivity(menuIntent, options.toBundle())
                    Log.d("customTag", "SettingActivity onCreate called; click menu button")
                    true
                }
                else -> false
            }
        }

        //자동 등록 관리
        binding.autoSet.setOnClickListener {
            val intent = Intent(this, AutoSettingActivity::class.java)
            startActivity(intent)
        }

        //중요 날짜 색상 변경
        binding.importantColor.setOnClickListener {
            val intent = Intent(this, ImportantColorActivity::class.java)
            startActivity(intent)
        }

        //사용자 지정 카테고리 관리
        binding.category.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }
    }
}