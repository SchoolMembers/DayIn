package com.schedule.dayin.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.AppController
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.MoneyRecyItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MoneyAdapter(private var dataList: MutableList<MoneyAndCate>, private val appController: AppController): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mainDb = appController.mainDb
    private var MoneyRepository = MoneyRepository(mainDb.moneyDbDao())
    private val uiScope = CoroutineScope(Dispatchers.Main)


    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MoneyViewHolder(MoneyRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MoneyViewHolder).binding



        binding.text.text = dataList[position].moneyDb.title




    }
}