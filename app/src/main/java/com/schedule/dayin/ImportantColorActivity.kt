package com.schedule.dayin

import android.app.Activity
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.schedule.dayin.databinding.ImportantColorActivityBinding

class ImportantColorActivity: AppCompatActivity() {

    private lateinit var binding: ImportantColorActivityBinding

    //중요 날짜 색상
    private fun saveImportantColor(color: Int) {
        val pref: SharedPreferences = this.getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putInt("color", color)
        editor.apply()
    }

    private fun checkImportantColor(): Int {
        val defaultColorList = ContextCompat.getColorStateList(this, R.color.pink)
        val defaultColor = defaultColorList!!.defaultColor
        val pref: SharedPreferences =  this.getSharedPreferences("pref", Activity.MODE_PRIVATE)
        return pref.getInt("color", defaultColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImportantColorActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
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

        //뒤로 가기
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.currentState.backgroundTintList = ColorStateList.valueOf(checkImportantColor())

        binding.star1.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.pink)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star2.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.red)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star3.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.yellow)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star4.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.green3)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star5.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.purple2)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star6.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.mint)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star7.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.blue2)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star8.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.orange2)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }

        binding.star9.setOnClickListener {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.hotPink)
            if (colorStateList != null) {
                val color = colorStateList.defaultColor
                binding.currentState.backgroundTintList = colorStateList
                saveImportantColor(color)
            }
        }
    }
}