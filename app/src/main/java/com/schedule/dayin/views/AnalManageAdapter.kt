package com.schedule.dayin.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.databinding.AnalysisManageItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class AnalManageAdapter(private var cateList: MutableList<CateDb>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = cateList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AnalManageViewHolder(AnalysisManageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AnalManageViewHolder).binding

        if (cateList.isEmpty()) {
            binding.cateText.text = "카테고리가 없습니다."
            binding.customText.text = ""
            return
        } else {
            binding.cateText.text = cateList[position].name

            if (cateList[position].cateId <= 25L) {
                binding.customText.text = "(기본)"
            } else {
                binding.customText.text = "(사용자 지정)"
            }
        }

    }
}