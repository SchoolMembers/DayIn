package com.schedule.dayin.views

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.databinding.UserCateItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserCateAdapter(private val context: Context, private var userCateList: Flow<List<CateDb>>, private var userMoneyList: Flow<List<MoneyAndCate>>, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var currentCateList: List<CateDb> = emptyList()

    private var currentList: List<MoneyAndCate> = emptyList()

    private var lastCateId: Long = -1L

    private var count = 0


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
                Log.d("customTag", "UserCateAdapter init: currentList updated with size: ${currentList.size}")
                notifyDataSetChanged()
            }
        }
    }

    //아이템 선택 포지션 리스트
    private val selectedPositions = mutableListOf<Int>()

    override fun getItemCount(): Int = currentCateList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserCateViewHolder(UserCateItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as UserCateViewHolder).binding

        Log.d("customTag", "UserCateAdapter onBindViewHolder called; position: $position")

        // 뷰 재사용 방지
        binding.cate.text = ""
        binding.autoMoneyCount.text = ""
        binding.moneyCount.text = ""

        val currentCateId = currentCateList[position].cateId

        //참이면 다른 카테고리 등장한 경우
        if (currentCateId != lastCateId) {
            lastCateId = currentCateId
            binding.cate.text = currentCateList[position].name
            binding.catePM.text = if (currentCateList[position].inEx == 0) "지출" else "수익"

            var money = 0
            var autoMoney = 0

            if (currentList.isNotEmpty()) {
                Log.d("customTag", "UserCateAdapter onBindViewHolder called; currentList not empty")
                for (i in count until currentList.size) {
                    if (currentList[i].moneyDb.cateId != currentCateId) break
                    money++
                    count++
                    Log.d(
                        "customTag",
                        "UserCateAdapter onBindViewHolder called; i: $i, tagcount: $count"
                    )
                    if (currentList[i].moneyDb.auto != 0) {
                        autoMoney++
                    }
                }
            }

            binding.autoMoneyCount.text = "$autoMoney 개"
            binding.moneyCount.text = "$money 개"

            Log.d(
                "customTag",
                "UserCateAdapter onBindViewHolder called; cateId: $currentCateId, money: $money, autoMoney: $autoMoney"
            )

        }

        //아이템 클릭 리스너
        binding.root.setOnClickListener {
            Log.d("customTag", "UserCateAdapter onBindViewHolder called; cateId: $currentCateId")
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position)
                binding.layout.background = AppCompatResources.getDrawable(context, R.drawable.time_back)
            } else {
                selectedPositions.add(position)
                binding.layout.background = AppCompatResources.getDrawable(context, R.drawable.time_back_red)
            }
            onDataChanged?.invoke()
        }
    }

    //선택한 카테고리들 반환하는 메서드
    fun getSelectedCategory(): List<CateDb>? {
        return if (selectedPositions.isNotEmpty()) selectedPositions.map { currentCateList[it] } else null
    }
}