package com.schedule.dayin


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import kotlinx.coroutines.flow.Flow

class MemoItemEditActivity : AppCompatActivity() {

    private lateinit var binding: MemoEditActivityBinding
    private lateinit var memoRepository: MemoRepository
    private var memoList: MemoDb? = null

    private val uiScope = CoroutineScope(Dispatchers.Main)

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollEdit) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        // db
        val appController = this.application as? AppController
        val memoDb = appController?.memoDb
        memoDb?.let {
            memoRepository = MemoRepository(it.memoDbDao())
        } ?: run {
            Log.e("MemoItemEditActivity", "AppController or memoDb is null")
            return
        }

        // 아이디
        val id = intent.getLongExtra("id", -1)

        if (id == -1L) {
            Log.e("MemoItemEditActivity", "Invalid memo ID: $id")
            return
        }

        // 데이터 불러오기
        uiScope.launch {
            withContext(Dispatchers.IO) {
                memoList = memoRepository.getMemoById(id)
            }
            withContext(Dispatchers.Main) {
                memoList?.let {
                    binding.title.setText(it.title)
                    binding.des.setText(it.des)
                } ?: run {
                    Log.e("MemoItemEditActivity", "Memo not found for ID: $id")
                }
            }
        }

        // 제목 리스너
        var titleText: String = "제목 없음"
        binding.title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                titleText = s?.toString() ?: "제목 없음"
                Log.d("MemoItemEditActivity", "Title text changed: $titleText")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //내용 리스너
        var desText: String = "메모"
        binding.des.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                desText = s?.toString() ?: "메모"
                Log.d("MemoItemEditActivity", "des text changed: $desText")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //닫기
        binding.closeButton.setOnClickListener {
            Log.d("customTag", "MemoEditActivity onCreate called; click close button")
            finish()
        }

        //체크
        binding.checkButton.setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    memoList?.let {
                        memoRepository.updateMemo(MemoDb(id = it.id, title = titleText, des = desText))
                    }
                }
            }
            Log.d("customTag", "MemoEditActivity onCreate called; click check button")
            finish()
        }
    }
}