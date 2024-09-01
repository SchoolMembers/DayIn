package com.schedule.dayin


import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.view) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        //db
        val appController = this.application as AppController
        val memoDb = appController.memoDb
        val memoRepository = MemoRepository(memoDb.memoDbDao())
        val uiScope = CoroutineScope(Dispatchers.Main)


        //닫기
        binding.closeButton.setOnClickListener {
            finish()
        }

        //editText title
        var titleText: String = ""
        binding.title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                titleText = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //editText des
        var desText: String = ""
        binding.des.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                desText = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //저장
        binding.checkButton.setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {

                    memoRepository.insertMemo(
                        MemoDb(
                            title = titleText,
                            des = desText
                        )
                    )
                }
            }
            finish()
        }

    }

    override fun onPause() {
        super.onPause()
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        val memoIntent = Intent(this, MemoActivity::class.java)
        startActivity(memoIntent, options.toBundle())
    }
}