package com.schedule.dayin

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AnalysisActivityBinding
import com.schedule.dayin.views.AnalPagerAdapter
import com.schedule.dayin.views.AutoAnalAdapter
import com.schedule.dayin.views.ItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AnalysisActivity: AppCompatActivity() {

    private lateinit var binding: AnalysisActivityBinding

    //텍스트 버튼 클릭 초기화
    private var autoManage = false
    private lateinit var autoTextView: TextView

    //데이터
    private lateinit var appController: AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    //뷰페이저
    private lateinit var pagerAdapter: AnalPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = AnalysisActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appController = this.application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())

        //날짜 표시
        val barDate = binding.monYear
        val currentMonth = LocalDate.now()
        barDate.text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))

        //뷰페이저
        pagerAdapter = AnalPagerAdapter(this, currentMonth)

        binding.viewpager.adapter = pagerAdapter
        binding.viewpager.setCurrentItem(pagerAdapter.startingPosition, false)

        //페이지 전환 함수
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val offset = position - pagerAdapter.startingPosition
                val selectedDate = currentMonth.plusMonths(offset.toLong())
                barDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
            }
        })

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
        autoTextView = binding.autoManage
        autoTextView.setOnClickListener {
            if (!autoManage) {
                autoTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
                autoManage = true
                showAutoDialog()
                Log.d("customTag", "AnalysisActivity onCreate called; autoManage = true")
            }
        }

        //소비 분석 관리 클릭 리스너
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
            autoManage = false
            autoTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        //리사이클러 뷰 데코레이션 지정
        val verticalSpaceHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        ).toInt()

        val autoRecyclerView = dialogView.findViewById<RecyclerView>(R.id.autoRecyclerView)
        autoRecyclerView.layoutManager = LinearLayoutManager(this)

        autoDataLoad(autoRecyclerView)

        autoRecyclerView.addItemDecoration(ItemDecoration(verticalSpaceHeight))






        dialog.setOnDismissListener {
            autoManage = false
            autoTextView.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        dialog.show()
    }

    //고정 지출 데이터 불러오는 함수
    private fun autoDataLoad(autoRecyclerView: RecyclerView) {
        val autoList= mutableListOf<MoneyAndCate>()

        var title = ""

        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoMoney().forEach { money ->
                        if (title != money.moneyDb.title) {
                            title = money.moneyDb.title!!
                            autoList.add(money)
                        }
                    }
                }
                withContext(Dispatchers.Main){
                    autoRecyclerView.adapter = AutoAnalAdapter(autoList)
                }
            } catch (e: Exception) {
                Log.e("AutoData", "Error collecting auto data", e)
                autoList.clear()
            }

        }

    }

}