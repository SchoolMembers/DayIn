package com.example.dayin.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dayin.R

//CalendarFragment에서 사용
class CalendarAdapter(private val dayList: ArrayList<Pair<String, Boolean>>, private val type: Int): RecyclerView.Adapter<CalendarAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val dayText: TextView = itemView.findViewById(R.id.dayText) //dayText id (calendar_item_s.xml, calendar_item_m.xml, calendar_item_d.xml choice one)
    }

    //화면 설정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        //CalendarFragment에서 전달 받은 layoutType 식별자 값에 따른 레이아웃 설정
        val layout = when(type) {
            0 -> R.layout.calendar_item_s
            1 -> R.layout.calendar_item_m
            2 -> R.layout.calendar_item_d
            else -> Log.d("CalendarAdapter","layout error")
        }
        Log.d("CalendarAdapter", "layout: $layout")

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        //각 아이템 최소 높이 설정
        val minHeight: Int = calculateMinHeightForItem(parent)
        view.setMinimumHeight(minHeight)
        return ItemViewHolder(view)
    }

    //리사이클러 각 아이템 최소 높이 계산 함수 (안드로이드 기기의 크기에 따름)
    private fun calculateMinHeightForItem(parent: ViewGroup): Int {
        // 화면의 높이를 가져오기
        val displayMetrics = parent.context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        var count = 7

        if (itemCount / 7 == 6) {
            count = 8
        }

        Log.d("CalendarAdapter", "calculateMinHeightForItem complete")
        // 최소 높이 계산
        return screenHeight / count
    }

    //데이터 설정 (리사이클러 뷰 아이템 날짜 text 설정)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int){
        val (dayText, isOtherMonth) = dayList[holder.bindingAdapterPosition]
        holder.dayText.text = dayText

        if (isOtherMonth) {
            holder.dayText.setTextColor(Color.parseColor("#80000000")) // 반투명
        } else {
            holder.dayText.setTextColor(Color.parseColor("#FF000000")) // 불투명
        }
    }

    //데이터 리스트 사이즈 get 함수
    override fun getItemCount(): Int {
        return dayList.size
    }
}