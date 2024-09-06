package com.schedule.dayin.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.databinding.AnalysisCateItemsBinding

class AnalCateAdapter(private var dataList: MutableList<MoneyAndCate>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AnalCateViewHolder(AnalysisCateItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AnalCateViewHolder).binding

        binding.cate.text = dataList[position].cateDb.name

        binding.money.text = dataList[position].moneyDb.money.toString() + " 원"
    }
}