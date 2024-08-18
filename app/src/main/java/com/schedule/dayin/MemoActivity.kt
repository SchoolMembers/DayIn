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
import com.schedule.dayin.databinding.ActivityMainBinding
import com.schedule.dayin.databinding.MemoActivityBinding
import com.schedule.dayin.databinding.MemoItemBinding

class MemoActivity : AppCompatActivity() {

    private lateinit var binding: MemoActivityBinding
    private lateinit var item_binding: MemoItemBinding

    val Memobinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = MemoActivityBinding.inflate(layoutInflater)
        item_binding = MemoItemBinding.inflate(layoutInflater)
        setContentView(item_binding.root)
        setContentView(binding.root)

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

        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //bottom navigation click event
        val memoIntent = Intent(this, MemoActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)

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
                else -> false
            }
        }

        val adapter = MemoCustomAdapter()
        adapter.listData = loadData()
        binding.recyclerViewMemo.adapter = adapter

        binding.recyclerViewMemo.layoutManager = LinearLayoutManager(this)
    }

    private fun loadData(): MutableList<MemoData>{
        val data : MutableList<MemoData> = mutableListOf()

        for(no in 1..20) {
            val title = "예제 ${no} 입니다"
            val date = System.currentTimeMillis()
            val isChecked = false
            val memo = MemoData(no, title, date, isChecked)
            data.add(memo)
        }
        return data
    }
}