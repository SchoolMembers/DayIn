package com.schedule.dayin


import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
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
            finish()
        }

        //닫기
        binding.closeButton.setOnClickListener {
            finish()
        }

        // 하단 텍스트뷰 클릭 이벤트
        binding.color.setOnClickListener {
            binding.colorMenu.visibility = View.VISIBLE
            binding.color.setOnClickListener {
                binding.colorMenu.visibility = View.GONE
            }
        }

        binding.font.setOnClickListener {
            binding.fontMenu.visibility = View.VISIBLE
            binding.font.setOnClickListener {
                binding.fontMenu.visibility = View.GONE
            }
        }

        binding.style.setOnClickListener {
            binding.styleMenu.visibility = View.VISIBLE
            binding.style.setOnClickListener {
                binding.styleMenu.visibility = View.GONE
            }
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