package com.schedule.dayin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.databinding.MemoItemBinding
import java.text.SimpleDateFormat

class MemoCustomAdapter : RecyclerView.Adapter<Holder>() {
    var listData = mutableListOf<MemoData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = MemoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false);

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo = listData[position]
        holder.setMemo(memo)
    }
}

class Holder(val memobinding: MemoItemBinding): RecyclerView.ViewHolder(memobinding.root){

    init{
        memobinding.root.setOnClickListener{
            Toast.makeText(memobinding.root.context, "클릭된 아이템 = ${memobinding.textTitle.text}",
                Toast.LENGTH_LONG).show()
        }

        memobinding.memoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(memobinding.root.context, "CheckBox on", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(memobinding.root.context, "CheckBox off", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setMemo(memo: MemoData){
        memobinding.textTitle.text = memo.title
        memobinding.memoCheckBox.isChecked = memo.isChecked
    }
}