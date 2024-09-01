package com.schedule.dayin.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.databinding.CateRecyItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CateAdapter(private var cateList: Flow<List<CateDb>>, private var choice: Long? = null, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var currentList: List<CateDb> = emptyList()

    //선택한 카테고리 -1은 아무것도 선택 안함
    private var selectedCate: Int = -1

    init {
        uiScope.launch {
            cateList.collect { newList ->
                currentList = newList
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CateViewHolder(CateRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CateViewHolder).binding
        binding.text.text = currentList[position].name

        if (currentList[position].cateId >= 25) {
            binding.text.setTextColor(ContextCompat.getColor(binding.text.context, com.schedule.dayin.R.color.pink))
        }

        val layout = binding.layout

        //머니 데이터 수정 시
        if (choice != null) {
            if (currentList[position].cateId == choice) {
                choice = null
                selectedCate = holder.adapterPosition
            }

        }

        //카테고리 선택?
        if (position == selectedCate) {
            //사용자 지정 카테고리?
            if (currentList[position].cateId >= 25) {
                layout.background = ContextCompat.getDrawable(layout.context, com.schedule.dayin.R.drawable.time_back_enable)
                binding.text.setTextColor(ContextCompat.getColor(binding.text.context, com.schedule.dayin.R.color.pink))
            } else {
                layout.background = ContextCompat.getDrawable(layout.context, com.schedule.dayin.R.drawable.time_back_enable)
                binding.text.setTextColor(ContextCompat.getColor(layout.context, com.schedule.dayin.R.color.white))
            }

        } else {
            if (currentList[position].cateId >= 25) {
                layout.background = ContextCompat.getDrawable(layout.context, com.schedule.dayin.R.drawable.time_back)
                binding.text.setTextColor(ContextCompat.getColor(layout.context, com.schedule.dayin.R.color.pink))
            } else {
                layout.background = ContextCompat.getDrawable(layout.context, com.schedule.dayin.R.drawable.time_back)
                binding.text.setTextColor(ContextCompat.getColor(layout.context, com.schedule.dayin.R.color.black))
            }
        }

        //카테고리 클릭 리스너
        layout.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) { // 유효한 위치인지 확인
                // 선택된 위치 업데이트
                val previousSelectedPosition = selectedCate
                selectedCate = adapterPosition

                // 이전 선택된 아이템과 새로 선택된 아이템 갱신
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedCate)

                onDataChanged?.invoke()
            }
        }

    }

    //선택한 카테고리 반환 메서드
    fun getSelectedCategory(): CateDb? {
        return if (selectedCate != -1) currentList[selectedCate] else null
    }
}