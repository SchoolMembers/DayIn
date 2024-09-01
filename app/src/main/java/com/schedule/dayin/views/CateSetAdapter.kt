package com.schedule.dayin.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.databinding.CateSetItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CateSetAdapter(private val context: Context, private var userCateList: Flow<List<CateDb>>, private var userMoneyList: Flow<List<MoneyAndCate>>, private val onDataChanged: (() -> Unit)? = null, private val onCateDel: ((Int) -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var currentCateList: List<CateDb> = emptyList()

    private var currentList: List<MoneyAndCate> = emptyList()

    private var lastCateId: Long = -1L

    private var count = 0

    private var indexDel = 0


    init {
        //사용자 카테고리 리스트
        uiScope.launch {
            userCateList.collect { newList ->
                currentCateList = newList
                Log.d("customTag", "UserCateAdapter init: currentCateList updated with size: ${currentCateList.size}")
                notifyDataSetChanged()
            }
        }
        //사용자 돈 리스트
        uiScope.launch {
            userMoneyList.collect { newList ->
                currentList = newList
                notifyDataSetChanged()
            }
        }
    }

    //아이템 선택 포지션 리스트
    private val selectedPositions = mutableListOf<Int>()

    override fun getItemCount(): Int = currentCateList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CateSetViewHolder(CateSetItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CateSetViewHolder).binding

        val currentCateId = currentCateList[position].cateId
        Log.d("customTag", "CateSetAdapter onBindViewHolder called; position: $position; id: $currentCateId")
        Log.d("customTag", "CateSetAdapter onBindViewHolder called; currentList: $currentList")

        //참이면 다른 카테고리 등장한 경우
        if (currentCateId != lastCateId) {
            lastCateId = currentCateId
            binding.cate.text = currentCateList[position].name

            var money = 0
            var autoMoney = 0

            if (currentList.isNotEmpty()) {
                Log.d("customTag", "CateSetAdapter onBindViewHolder called; currentList not empty")
                for (i in count until currentList.size) {
                    if (currentList[i].moneyDb.cateId != currentCateId) break
                    money++
                    count++
                    if (currentList[i].moneyDb.auto != 0) {
                        autoMoney++
                    }
                }
            }
            Log.d("customTag", "CateSetAdapter onBindViewHolder called; cateId: $currentCateId; money: $money; autoMoney: $autoMoney")
            binding.autoMoneyCount.text = "$autoMoney 개"
            binding.moneyCount.text = "$money 개"
        }

        //아이템 클릭 리스너
        binding.root.setOnClickListener {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position)
                binding.layout.background = AppCompatResources.getDrawable(context, R.drawable.time_back)
            } else {
                selectedPositions.add(position)
                binding.layout.background = AppCompatResources.getDrawable(context, R.drawable.time_back_red)
            }
            if (selectedPositions.isNotEmpty()) {
                indexDel = 1
            } else {
                indexDel = 0
            }
            onDataChanged?.invoke()
            onCateDel?.invoke(indexDel)
        }
    }

    //선택한 카테고리들 반환하는 메서드
    fun getSelectedCategory(): List<CateDb>? {
        return if (selectedPositions.isNotEmpty()) selectedPositions.map { currentCateList[it] } else null
    }
}