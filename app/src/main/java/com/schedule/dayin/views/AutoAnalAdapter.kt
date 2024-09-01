package com.schedule.dayin.views


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.databinding.AutoAnalRecyItemsBinding

class AutoAnalAdapter(private var dataList: MutableList<MoneyAndCate>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AutoAnalViewHolder(AutoAnalRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val binding = (holder as AutoAnalViewHolder).binding

        //카테고리 이름 설정
        val cateName = binding.cate
        cateName.text = dataList[position].cateDb.name

        //카테고리 속성 설정
        val catePm = binding.catePM
        catePm.text = if (dataList[position].cateDb.inEx == 0) "지출" else "수익"

        //제목 설정
        val title = binding.title
        title.text = dataList[position].moneyDb.title

        //금액 설정
        val money = binding.money
        money.text = dataList[position].moneyDb.money.toString() + " 원"
    }
}