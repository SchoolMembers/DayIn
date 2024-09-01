package com.schedule.dayin

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.dayin.data.memoD.repository.MemoRepository
import com.schedule.dayin.databinding.MemoActivityBinding
import com.schedule.dayin.views.MemoCustomAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MemoActivity : AppCompatActivity() {

    private lateinit var binding: MemoActivityBinding

    //리사이클러
    private lateinit var adapter: MemoCustomAdapter
    private var dataList = mutableListOf<Triple<Long, String, String>>()
    private var checkList = mutableListOf<Long>()
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var memoRepository: MemoRepository



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = MemoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //db
        val appController = this.application as AppController
        val memoDb = appController.memoDb
        memoRepository = MemoRepository(memoDb.memoDbDao())

        //하단 바 활성화 상태
        binding.bottomNavigation.selectedItemId = R.id.barMemo

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

        //리사이클러 데이터 로드
        uiScope.launch {
            loadData()
        }

        adapter = MemoCustomAdapter(this, dataList, checkList)
        binding.recyclerViewMemo.adapter = adapter
        binding.recyclerViewMemo.layoutManager = LinearLayoutManager(this)

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
                    Log.d("customTag", "MemoActivity onCreate called; click home button")
                    true
                }
                R.id.barMemo -> {
                    startActivity(memoIntent, options.toBundle())
                    Log.d("customTag", "MemoActivity onCreate called; click memo button")
                    true
                }
                R.id.barMoney -> {
                    startActivity(analysisIntent, options.toBundle())
                    Log.d("customTag", "MemoActivity onCreate called; click analysis button")
                    true
                }
                R.id.barMenu -> {
                    startActivity(menuIntent, options.toBundle())
                    Log.d("customTag", "MemoActivity onCreate called; click menu button")
                    true
                }
                else -> false
            }
        }


        //메모 추가
        binding.btnPlus.setOnClickListener {
            val intent = Intent(this, MemoEditActivity::class.java)
            startActivity(intent, options.toBundle())
            Log.d("customTag", "MemoActivity onCreate called; click plus button")
        }

    }



    //메모 데이터 불러오는 함수
    private fun loadData() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                memoRepository.getMemoTitles().collect { memoList ->
                    dataList.clear()
                    memoList.forEach { memo ->
                        dataList.add(Triple(memo.id, memo.title, memo.des))
                    }
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

}