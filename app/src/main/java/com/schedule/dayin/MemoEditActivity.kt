package com.schedule.dayin

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.memoD.MemoDb
import com.schedule.dayin.data.memoD.repository.MemoRepository
import com.schedule.dayin.databinding.MemoEditActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemoEditActivity : AppCompatActivity() {

    private lateinit var binding: MemoEditActivityBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = MemoEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.memoEdit) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // 하단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        //db
        val appController = this.application as AppController
        val memoDb = appController.memoDb
        val memoRepository = MemoRepository(memoDb.memoDbDao())
        val uiScope = CoroutineScope(Dispatchers.Main)

        //화면 전환 animation setting
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        //저장
        binding.checkButton.setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {

                    memoRepository.insertMemo(
                        MemoDb(
                            title = binding.title.text.toString(),
                            des = binding.des.text.toString()
                        )
                    )
                }
            }
            val intent = Intent(this, MemoActivity::class.java)
            startActivity(intent, options.toBundle())
            Log.d("customTag", "MemoEditActivity onCreate called; click check button")
            finish()
        }

        //닫기
        binding.closeButton.setOnClickListener {
            val intent = Intent(this, MemoActivity::class.java)
            startActivity(intent, options.toBundle())
            Log.d("customTag", "MemoEditActivity onCreate called; click close button")
            finish()
        }

        // 예제 HTML 텍스트
        val htmlText = "This is <font color=#0000FF>blue</font> and <i>italic</i>."

        // HTML 텍스트를 Spanned로 변환하여 EditText에 설정
        binding.des.setText(htmlText.toSpanned())
    }

    // 확장 함수: String을 Spanned로 변환
    private fun String.toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(this)
        }
    }
}