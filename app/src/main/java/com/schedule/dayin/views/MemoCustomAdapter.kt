package com.schedule.dayin.views


import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.MemoItemEditActivity
import com.schedule.dayin.databinding.MemoItemBinding

class MemoCustomAdapter(private val context: Context,  private val memoList: MutableList<Triple<Long, String, String>>, private val onDel: ((Long) -> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //memoList: 메모 데이터들
    //checkList: 첫번째 값이 0이면 전체 선택 x, 1이면 전체 선택 o. 그 다음 값들 부터는 체크된 메모 id


    //아이템 선택 포지션 리스트
    private val selectedPositions = mutableListOf<Long>()

    override fun getItemCount(): Int = memoList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemoViewHolder(MemoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MemoViewHolder).binding

        binding.textTitle.text = memoList[position].second

        //리사이클러 아이템 클릭 이벤트
        binding.memoLayout.setOnClickListener {
            //화면 전환 animation setting
            if (context is Activity) {
                // 화면 전환 animation setting
                val options = ActivityOptions.makeCustomAnimation(context, 0, 0)
                val intent = Intent(context, MemoItemEditActivity::class.java)
                intent.putExtra("id", memoList[position].first)
                context.startActivity(intent, options.toBundle())
                Log.d("customTag", "MemoCustomAdapter onCreate called; click Item: $position")
            } else {
                Log.e("customTag", "MemoCustomAdapter onCreate: context is not an Activity")
            }
        }

        //제목 없으면 (임시)
        if (memoList[position].second == "") {
            binding.textTitle.text = "제목 없음"
        }

        //체크박스
        binding.memoCheckBox.isChecked = selectedPositions.contains(memoList[position].first)
        binding.memoCheckBox.setOnClickListener {
            if (binding.memoCheckBox.isChecked) {
                selectedPositions.add(memoList[position].first)
            } else {
                selectedPositions.remove(memoList[position].first)
            }
            onDel?.invoke(memoList[position].first)
        }
    }

    //선택한 항목 반환하는 메서드
    fun getSelected(): List<Long>? {
        return if (selectedPositions.isNotEmpty()) selectedPositions.toList() else null
    }
}