package com.schedule.dayin.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.contains
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.schedule.dayin.MainActivity
import com.schedule.dayin.databinding.FragmentSBinding
import com.schedule.dayin.databinding.ScheduleRecyItemsBinding
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class ScheduleAdapter(private val context: Context, private val dataList: MutableList<Triple<Long, String, String>>, private val clickCheck: Boolean): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("pref", Activity.MODE_PRIVATE)
    }

    // SharedPreferences Editor 객체
    private val editor: SharedPreferences.Editor by lazy {
        sharedPreferences.edit()
    }

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ScheduleViewHolder(ScheduleRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val  binding = (holder as ScheduleViewHolder).binding



        binding.text.text = dataList[position].second

        // 체크박스 초기 상태 설정
        val id = dataList[position].first
        val isChecked = loadCheck(id) == 1
        binding.check.isChecked = isChecked

        //레이아웃 디자인 변경
        if (dataList[position].third == "") {
            binding.time.visibility = ViewGroup.GONE
        } else {
            binding.time.text = dataList[position].third
            binding.time.visibility = ViewGroup.VISIBLE
        }

        //dp 단위 px로 변환
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }



        val layoutParams = binding.layout.layoutParams as ViewGroup.MarginLayoutParams

        //날짜 칸 누른 상태일 때
        if (clickCheck) {
            binding.text.maxLines = 10

            //레이아웃 클릭
            binding.layout.isClickable = true
            binding.layout.setOnClickListener {
                if (clickCheck){
                    Log.d("customTag", "ScheduleAdapter onBindViewHolder called; item clicked")
                }
            }

            binding.check.visibility = ViewGroup.VISIBLE
            binding.time.visibility = ViewGroup.VISIBLE

            val marginInPx = dpToPx(5)
            layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            binding.layout.layoutParams = layoutParams
        }
        //달력 상태일 때
        else {
            binding.text.maxLines = 1

            binding.layout.isClickable = false

            binding.check.visibility = ViewGroup.GONE
            binding.time.visibility = ViewGroup.GONE
            val marginInPx = dpToPx(0)
            layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            binding.layout.layoutParams = layoutParams
        }



        //체크리스트 클릭
        binding.check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                saveCheck(id, true)
            } else {
                saveCheck(id, false)
            }
        }

    }
    // 체크 상태 저장
    private fun saveCheck(id: Long, check: Boolean) {
        val idString = id.toString()
        val result = if (check) 1 else 0
        editor.putInt(idString, result)
        editor.commit()
    }

    // 체크 상태 로드
    private fun loadCheck(id: Long): Int {
        val idString = id.toString()
        return sharedPreferences.getInt(idString, 0)
    }

}