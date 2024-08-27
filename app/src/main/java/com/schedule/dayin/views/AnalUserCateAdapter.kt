package com.schedule.dayin.views

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.databinding.AnalysisUserCateItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AnalUserCateAdapter(private val context: Context, private var userCateList: List<CateDb>, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private lateinit var binding: AnalysisUserCateItemsBinding

    private fun saveCategory(category: Long, index: Long) {
        val cateString = category.toString()
        val pref: SharedPreferences = context.getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putLong(cateString, index)
        editor.apply()
    }

    // 0: 식비 1: 패션/미용 2: 음료/주류 3: 교통 4: 의료/건강 5: 주거 6: 교육 7: 여가 8: 생활 9: 기타
    private fun loadCategory(category: Long): Long {
        val cateString = category.toString()
        val pref: SharedPreferences = context.getSharedPreferences("anal", Activity.MODE_PRIVATE)
        return pref.getLong(cateString, -1L)
    }

    private fun removeCategory(category: Long) {
        val cateString = category.toString()
        val pref: SharedPreferences = context.getSharedPreferences("anal", Activity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.remove(cateString)
        editor.apply()
    }

    //데이터 뷰 적용
    fun updateData() {
        notifyDataSetChanged()
        onDataChanged?.invoke()
    }

    override fun getItemCount(): Int = userCateList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AnalUserCateViewHolder(AnalysisUserCateItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        binding = (holder as AnalUserCateViewHolder).binding

        binding.cateText.text = userCateList[position].name

        val indexId = loadCategory(userCateList[position].cateId)

        Log.d("loadCate", "AnalUserCateAdapter onBindViewHolder called; indexId: $indexId")

        if (indexId != -1L) {
            binding.indexText.visibility = View.VISIBLE
            binding.indexText.text = when (indexId) {
                0L -> "식비"
                1L -> "패션/미용"
                2L -> "음료/주류"
                3L -> "교통"
                4L -> "의료/건강"
                5L -> "주거"
                6L -> "교육"
                7L -> "여가"
                8L -> "생활"
                9L -> "기타"
                else -> "기타"
            }
        } else {
            binding.indexText.visibility = View.INVISIBLE
            binding.indexText.text = ""
        }


        binding.layout.setOnClickListener {
            clickItemDialog(userCateList[position].cateId)
        }
    }

    private fun clickItemDialog(cateId: Long) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.anal_user_cate_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
        val dialog = dialogBuilder.create()



        //닫기 버튼
        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        var index: Long

        //식비
        dialogView.findViewById<LinearLayout>(R.id.cate0).setOnClickListener {
            index = 0
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 식비 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //패션/미용
        dialogView.findViewById<LinearLayout>(R.id.cate1).setOnClickListener {
            index = 1
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 패션/미용 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //음료/주류
        dialogView.findViewById<LinearLayout>(R.id.cate2).setOnClickListener {
            index = 2
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 음료/주류 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //교통
        dialogView.findViewById<LinearLayout>(R.id.cate3).setOnClickListener {
            index = 3
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 교통 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //의료/건강
        dialogView.findViewById<LinearLayout>(R.id.cate4).setOnClickListener {
            index = 4
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 의료/건강 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //주거
        dialogView.findViewById<LinearLayout>(R.id.cate5).setOnClickListener {
            index = 5
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 주거 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //교육
        dialogView.findViewById<LinearLayout>(R.id.cate6).setOnClickListener {
            index = 6
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 교육 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //여가
        dialogView.findViewById<LinearLayout>(R.id.cate7).setOnClickListener {
            index = 7
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 여가 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //생활
        dialogView.findViewById<LinearLayout>(R.id.cate8).setOnClickListener {
            index = 8
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 생활 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //기타
        dialogView.findViewById<LinearLayout>(R.id.cate9).setOnClickListener {
            index = 9
            saveCategory(cateId, index)
            Toast.makeText(context, "파이 그래프의 기타 범주로 설정되었습니다.", Toast.LENGTH_SHORT).show()
            updateData()
            dialog.dismiss()
        }

        //제거
        dialogView.findViewById<TextView>(R.id.remove).setOnClickListener {
            removeCategory(cateId)
            updateData()
            dialog.dismiss()
        }

        dialog.show()
    }
}