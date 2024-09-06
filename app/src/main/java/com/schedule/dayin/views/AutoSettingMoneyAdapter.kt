package com.schedule.dayin.views

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.AutoSettingItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class AutoSettingMoneyAdapter(private val context: Context, private val appController: AppController, private var dataList: MutableList<MoneyAndCate>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mainDb: MainDatabase
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var moneyRepository: MoneyRepository

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AutoSettingViewHolder(AutoSettingItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AutoSettingViewHolder).binding

        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())

        binding.title.text = dataList[position].moneyDb.title

        if (dataList[position].moneyDb.auto == 1) {
            binding.day.text = SimpleDateFormat("매주 EEEE", Locale.KOREAN).format(dataList[position].moneyDb.date)
        } else if (dataList[position].moneyDb.auto == 2) {
            binding.day.text = SimpleDateFormat("매달 dd일", Locale.KOREAN).format(dataList[position].moneyDb.date)
        } else {
            binding.day.text = SimpleDateFormat("매년 MM월 dd일", Locale.KOREAN).format(dataList[position].moneyDb.date)
        }

        if (dataList[position].cateDb.inEx == 0) {
            binding.money.text = "- " + dataList[position].moneyDb.money.toString()
            binding.money.setTextColor(ContextCompat.getColor(binding.money.context, R.color.red))
        } else {
            binding.money.text = "+ " + dataList[position].moneyDb.money.toString()
            binding.money.setTextColor(ContextCompat.getColor(binding.money.context, R.color.green3))
        }

        binding.money.visibility = View.VISIBLE

        binding.layout.setOnClickListener {
            showItemDialog(dataList[position], position)
        }

    }

    private fun showItemDialog(data: MoneyAndCate, position: Int) {
        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.auto_setting_sche_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.title).text = data.moneyDb.title

        //날짜 표시
        if (data.moneyDb.auto == 1) {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매주 EEEE", Locale.KOREAN).format(data.moneyDb.date)
        } else if (data.moneyDb.auto == 2) {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매달 dd일", Locale.KOREAN).format(data.moneyDb.date)
        } else {
            dialogView.findViewById<TextView>(R.id.date).text = SimpleDateFormat("매년 MM월 dd일", Locale.KOREAN).format(data.moneyDb.date)
        }

        //금액 표시
        dialogView.findViewById<TextView>(R.id.time).text = "금액"
        if (data.cateDb.inEx == 0) {
            dialogView.findViewById<TextView>(R.id.timeText).text = "-" + data.moneyDb.money.toString()
            dialogView.findViewById<TextView>(R.id.timeText).setTextColor(ContextCompat.getColor(dialogView.findViewById<TextView>(R.id.timeText).context, R.color.red))
        } else {
            dialogView.findViewById<TextView>(R.id.timeText).text = "+" + data.moneyDb.money.toString()
            dialogView.findViewById<TextView>(R.id.timeText).setTextColor(ContextCompat.getColor(dialogView.findViewById<TextView>(R.id.timeText).context, R.color.green3))
        }

        //데이터 불러오기
        uiScope.launch {
            var moneys: List<MoneyAndCate>
            withContext(Dispatchers.IO) {
                moneys = moneyRepository.getAutoTitle(data.moneyDb.title!!)
            }
            withContext(Dispatchers.Main) {
                if (moneys.isNotEmpty()) {
                    dialogView.findViewById<TextView>(R.id.startDate).text =
                        SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(moneys.first().moneyDb.date)
                    dialogView.findViewById<TextView>(R.id.endDate).text =
                        SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(moneys.last().moneyDb.date)
                }

            }
        }

        //삭제 버튼
        dialogView.findViewById<TextView>(R.id.deleteButton).setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    moneyRepository.deleteAuto(data.moneyDb.title!!)
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