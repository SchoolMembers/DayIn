package com.example.dayin.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dayin.MainActivity
import com.example.dayin.R

class CalendarAdapter(private val dayList: ArrayList<String>, private val type: Int): RecyclerView.Adapter<CalendarAdapter.ItemViewHolder>() {


    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }

    //화면 설정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        var layout = when(type) {
            0 -> R.layout.calendar_item_s
            1 -> R.layout.calendar_item_m
            2 -> R.layout.calendar_item_d
            else -> Log.d("CalendarAdapter","layout error")
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        //최소 높이 설정
        val minHeight: Int = calculateMinHeightForItem(parent)
        view.setMinimumHeight(minHeight)
        return ItemViewHolder(view)
    }

    private fun calculateMinHeightForItem(parent: ViewGroup): Int {
        // 화면의 높이를 가져오기
        val displayMetrics = parent.context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // 최소 높이 계산
        return screenHeight / 8
    }

    //데이터 설정
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int){
        holder.dayText.text = dayList[holder.adapterPosition]
    }

    override fun getItemCount(): Int {
        return dayList.size
    }
}