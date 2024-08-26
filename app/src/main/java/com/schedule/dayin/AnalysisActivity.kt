package com.schedule.dayin

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AnalysisActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisActivity: AppCompatActivity() {

    private lateinit var binding: AnalysisActivityBinding

    //텍스트 버튼 클릭 초기화
    private var autoManage = false

    //데이터
    private val appController = application as AppController
    private var mainDb = appController.mainDb
    private var moneyRepository = MoneyRepository(mainDb.moneyDbDao())
    private val uiScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = AnalysisActivityBinding.inflate(layoutInflater)
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
        binding.bottomNavigation.selectedItemId = R.id.barMoney

        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //bottom navigation click event
        val memoIntent = Intent(this, MemoActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)
        val analysisIntent = Intent(this, AnalysisActivity::class.java)

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.barHome -> {
                    startActivity(homeIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click home button")
                    true
                }
                R.id.barMemo -> {
                    startActivity(memoIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click memo button")
                    true
                }
                R.id.barMoney -> {
                    startActivity(analysisIntent, options.toBundle())
                    Log.d("customTag", "AnalysisActivity onCreate called; click analysis button")
                    true
                }
                else -> false
            }
        }

        //고정 지출 클릭 리스너
        val autoTextView = binding.autoManage
        autoTextView.setOnClickListener {
            if (!autoManage) {
                autoTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
                autoManage = true
                Log.d("customTag", "AnalysisActivity onCreate called; autoManage = true")
            }
        }
    }

    //고정 지출 다이얼로그
    private fun showAutoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_anal_auto, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        //info 버튼 클릭 리스너
        val info = dialogView.findViewById<Button>(R.id.infoButton)
        info.setOnClickListener {
            Toast.makeText(this, "자동 등록 된 데이터들 입니다! 메뉴의 자동 등록 관리에서 관리할 수 있어요.", Toast.LENGTH_SHORT).show()
        }

        //닫기 버튼 클릭 리스너
        val close = dialogView.findViewById<Button>(R.id.closeButton)
        close.setOnClickListener {
            dialog.dismiss()
        }
    }

    //고정 지출 데이터 불러오는 함수
    private fun autoDataLoad(): MutableList<MoneyAndCate> {
        var autoList= mutableListOf<MoneyAndCate>()

        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoMoney().forEach { money ->
                        autoList.add(money)
                    }
                }
            } catch (e: Exception) {
                Log.e("AutoData", "Error collecting auto data", e)
                autoList.clear()
            }

        }

        return autoList
    }

}