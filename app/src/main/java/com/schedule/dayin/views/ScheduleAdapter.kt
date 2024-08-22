package com.schedule.dayin.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.ScheduleRepository
import com.schedule.dayin.databinding.ScheduleRecyItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleAdapter(private val context: Context, private var dataList: MutableList<ScheduleDb>, private val clickCheck: Boolean, private val appController: AppController, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("pref", Activity.MODE_PRIVATE)
    }

    private var mainDb = appController.mainDb
    private  var scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())
    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun updateData(newData: MutableList<ScheduleDb>) {
        dataList = newData
        notifyDataSetChanged()
    }

    fun formatDate(date: Date?): String {
        return date?.let {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
            dateFormat.format(it)
        } ?: "" // null인 경우 빈 문자열 반환
    }

    fun formatDateKo(date: Date?): String {
        return date?.let {
            val dateFormat = SimpleDateFormat("HH시 mm분", Locale.KOREAN)
            dateFormat.format(it)
        } ?: "" // null인 경우 빈 문자열 반환
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
        val binding = (holder as ScheduleViewHolder).binding



        binding.text.text = dataList[position].title

        // 체크박스 초기 상태 설정
        val id = dataList[position].id
        val isChecked = loadCheck(id) == 1
        binding.check.isChecked = isChecked

        //시간 포매팅
        val time: String
        if (dataList[position].time != 0) {
            time = formatDate(dataList[position].date)
        } else {
            time = ""
        }

        //레이아웃 디자인 변경
        if (time == "") {
            binding.time.visibility = ViewGroup.GONE
        } else {
            binding.time.text = time
            binding.time.visibility = ViewGroup.VISIBLE
        }

        //색상 변경
        //colorDefault(lightGray) : 0 | colorGray : 1 | colorYellow : 2 | colorPurple : 3 | colorBlue : 4 | colorGreen 5
        when (dataList[position].color) {
            0 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_light_gray)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            1 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_dark_gray)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.time.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            2 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_yellow)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            3 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_purple)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            4 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_blue)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            5 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_green)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            6 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_red)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
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
            binding.text.isClickable = true
            //아이템 터치 리스너
            binding.text.setOnClickListener {

                Log.d("customTag", "ScheduleAdapter onBindViewHolder called; item clicked")

                showItemDialog(dataList[position], position)

            }

            if (time == "") {
                binding.time.visibility = ViewGroup.GONE
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
            binding.text.isClickable = false

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

    //자세히 보기 다이얼로그
    private fun showItemDialog(data: ScheduleDb, position: Int) {

        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.item_dialog_s, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.title).text = data.title

        if (data.memo != null) {
            dialogView.findViewById<TextView>(R.id.memoText).text = data.memo
        }

        dialogView.findViewById<TextView>(R.id.time).text = when (data.time) {
            1 -> formatDateKo(data.date)
            else -> "없음"
        }

        dialogView.findViewById<TextView>(R.id.notify).text = when (data.notify) {
            1 -> "하루 전"
            2 -> "1시간 전"
            3 -> "30분 전"
            else -> "없음"
        }

        dialogView.findViewById<TextView>(R.id.auto).text = when (data.auto) {
            1 -> "매주"
            2 -> "매달"
            3 -> "매년"
            else -> "없음"
        }

        //수정
        dialogView.findViewById<Button>(R.id.editButton).setOnClickListener {
            dialog.dismiss()
        }

        //삭제
        dialogView.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    scheduleRepository.deleteSche(data)
                }
                withContext(Dispatchers.Main) {

                    dataList.removeAt(position)

                    notifyItemRemoved(position)

                    //캘린더 셀 콜백
                    onDataChanged?.invoke()

                    dialog.dismiss()
                }
            }
        }

        //닫기
        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        // 다이얼로그 표시
        dialog.show()

    }
}