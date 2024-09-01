package com.schedule.dayin.views

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.ScheduleRepository
import com.schedule.dayin.databinding.AutoSettingItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class AutoSettingScheAdapter(private val context: Context, private val appController: AppController, private var dataList: MutableList<ScheduleDb>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mainDb: MainDatabase
    private lateinit var scheduleRepository: ScheduleRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AutoSettingViewHolder(AutoSettingItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AutoSettingViewHolder).binding

        mainDb = appController.mainDb
        scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())

        binding.title.text = dataList[position].title

        if (dataList[position].auto == 1) {
            binding.day.text = SimpleDateFormat("매주 EEEE", Locale.KOREAN).format(dataList[position].date)
        } else if (dataList[position].auto == 2) {
            binding.day.text = SimpleDateFormat("매달 dd일", Locale.KOREAN).format(dataList[position].date)
        } else {
            binding.day.text = SimpleDateFormat("매년 MM월 dd일", Locale.KOREAN).format(dataList[position].date)
        }

        binding.layout.setOnClickListener {
            showItemDialog(dataList[position], position)
        }

    }

    private fun showItemDialog(data: ScheduleDb, position: Int) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.auto_setting_sche_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.title).text = data.title

        //날짜 표시
        if (data.auto == 1) {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매주 EEEE", Locale.KOREAN).format(data.date)
        } else if (data.auto == 2) {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매달 dd일", Locale.KOREAN).format(data.date)
        } else {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매년 MM월 dd일", Locale.KOREAN).format(data.date)
        }

        //시간 등록 표시
        if (data.time == 1) {
            dialogView.findViewById<TextView>(R.id.timeText).text = SimpleDateFormat("HH:mm", Locale.KOREAN).format(data.date)
        }

        //데이터 불러오기
        uiScope.launch {
            var schedules: List<ScheduleDb>
            withContext(Dispatchers.IO) {
                schedules = scheduleRepository.getAutoTitle(data.title, data.auto)
            }
            withContext(Dispatchers.Main) {
                if (schedules.isNotEmpty()) {
                    dialogView.findViewById<TextView>(R.id.startDate).text =
                        SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(schedules.first().date)
                    dialogView.findViewById<TextView>(R.id.endDate).text =
                        SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(schedules.last().date)
                }

            }
        }

        //삭제 버튼
        dialogView.findViewById<TextView>(R.id.deleteButton).setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    scheduleRepository.deleteAuto(data.title, data.auto)
                }
                withContext(Dispatchers.Main) {

                    dataList.removeAt(position)

                    notifyDataSetChanged()

                    dialog.dismiss()
                }
            }
        }

        //닫기 버튼
        dialogView.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }
}