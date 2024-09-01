package com.schedule.dayin

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.CategoryActivityBinding
import com.schedule.dayin.views.CateSetAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryActivity: AppCompatActivity() {

    private lateinit var binding: CategoryActivityBinding

    private lateinit var appController : AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private lateinit var cateRepository: CateRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private lateinit var cateList1: Flow<List<CateDb>>
    private lateinit var cateList2: Flow<List<CateDb>>

    private lateinit var moneyList1: Flow<List<MoneyAndCate>>
    private lateinit var moneyList2: Flow<List<MoneyAndCate>>

    private lateinit var cateAdapter1: CateSetAdapter
    private lateinit var cateAdapter2: CateSetAdapter

    private lateinit var cateRecyclerView1: RecyclerView
    private lateinit var cateRecyclerView2: RecyclerView

    private var delCates1: List<CateDb>? = null
    private var delCates2: List<CateDb>? = null
    private lateinit var delButton: TextView

    private var indexDel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CategoryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appController = this.application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())
        cateRepository = CateRepository(mainDb.cateDao())

        binding.backButton.setOnClickListener {
            finish()
        }

        //카테고리 추가
        binding.catePlus.setOnClickListener {
            addCateDialog()
            binding.catePlus.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        //카테고리 삭제
        delButton = binding.cateMinus
        if (indexDel == 0) {
            delButton.setTextColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            delButton.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        delButton.setOnClickListener {
            if (indexDel == 0) {
                delButton.setTextColor(ContextCompat.getColor(this, R.color.black))
                binding.delText.visibility = View.VISIBLE
                indexDel = 1
            } else {
                binding.delText.visibility = View.GONE
                if (delCates1 != null) {
                    uiScope.launch {
                        withContext(Dispatchers.IO) {
                            delCates1!!.forEach { cate ->
                                moneyRepository.deleteMoneyByCateId(cate.cateId)
                            }
                            delCates1!!.forEach { cate ->
                                cateRepository.deleteCateById(cate.cateId)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            loadCate(0)
                            delButton.setTextColor(ContextCompat.getColor(this@CategoryActivity, R.color.gray))
                            delCates1 = null
                        }
                    }
                }
                if (delCates2 != null) {
                    uiScope.launch {
                        withContext(Dispatchers.IO) {
                            delCates2!!.forEach { cate ->
                                moneyRepository.deleteMoneyByCateId(cate.cateId)
                            }
                            delCates2!!.forEach { cate ->
                                cateRepository.deleteCateById(cate.cateId)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            loadCate(1)
                            delButton.setTextColor(ContextCompat.getColor(this@CategoryActivity, R.color.gray))
                            delCates2 = null
                        }
                    }
                }
                if (delCates1 == null && delCates2 == null) {
                    delButton.setTextColor(ContextCompat.getColor(this, R.color.gray))
                }
                indexDel = 0
            }
        }

        cateRecyclerView1 = binding.minus
        cateRecyclerView2 = binding.plus

        cateRecyclerView1.layoutManager = LinearLayoutManager(this)
        cateRecyclerView2.layoutManager = LinearLayoutManager(this)

        loadCate(0)
        loadCate(1)
    }

    //카테고리 추가 다이얼로그
    private fun addCateDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_c_add, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        var addInEx = 0

        //체크 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.isEnabled = false
        checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)

        //카테고리 이름 입력
        val cateName = dialogView.findViewById<TextView>(R.id.editText)
        var cateText: String = ""

        cateName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cateText = s?.toString() ?: ""

                if (cateText == "") {
                    checkButton.isEnabled = false
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.false_check_icon, null)
                } else {
                    checkButton.isEnabled = true
                    checkButton.background = ResourcesCompat.getDrawable(resources, R.drawable.save_icon, null)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //소비 지출 토글
        val cateToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.cateToggle)
        cateToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                addInEx = when (checkedId) {
                    R.id.minus -> 0
                    R.id.plus -> 1
                    else -> 0
                }
            }
        }

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            binding.catePlus.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        //체크 버튼 클릭
        checkButton.setOnClickListener {
            if (cateText == "") {
                Toast.makeText(this, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    cateRepository.insertCate(CateDb(name = cateText, inEx = addInEx))
                    loadCate(addInEx)
                }
            }

            dialog.dismiss()
        }

        //다이얼로그가 닫힐 때
        dialog.setOnDismissListener {
            binding.catePlus.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }

        dialog.show()
    }

    //카테고리 데이터 로드
    private fun loadCate(inEx: Int) {
        uiScope.launch {
            if (inEx == 0) {
                moneyList1 = moneyRepository.getMoneyId(0)
                cateList1 = cateRepository.getUserCateInex2(inEx)

                cateList1.collect { list ->
                    if (list.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.noData1.visibility = View.GONE
                            cateRecyclerView1.visibility = View.VISIBLE
                            cateAdapter1 = CateSetAdapter(this@CategoryActivity, cateList1, moneyList1,
                                onDataChanged = { cateDel(0) }, onCateDel = { choice(it) })
                            cateRecyclerView1.adapter = cateAdapter1
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.noData1.visibility = View.VISIBLE
                            cateRecyclerView1.visibility = View.GONE
                        }
                    }
                }

            } else if (inEx == 1) {
                moneyList2 = moneyRepository.getMoneyId(1)
                cateList2 = cateRepository.getUserCateInex2(inEx)

                cateList2.collect { list ->
                    if (list.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.noData2.visibility = View.GONE
                            cateRecyclerView2.visibility = View.VISIBLE
                            cateAdapter2 = CateSetAdapter(this@CategoryActivity, cateList2, moneyList2,
                                onDataChanged = { cateDel(1) }, onCateDel = { choice(it) })
                            cateRecyclerView2.adapter = cateAdapter2
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.noData2.visibility = View.VISIBLE
                            cateRecyclerView2.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    //카테고리 삭제 리스트 등록 함수
    private fun cateDel(index: Int) {
        if (index == 0) {
            delCates1 = cateAdapter1.getSelectedCategory()

        } else {
            delCates2 = cateAdapter2.getSelectedCategory()
        }

    }

    //삭제 아이템 선택
    private fun choice(index: Int) {
        indexDel = index
        if (index == 1 || delCates1 != null || delCates2 != null) {
            delButton.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.delText.visibility = View.VISIBLE
            indexDel = 1
        } else {
            delButton.setTextColor(ContextCompat.getColor(this, R.color.gray))
            binding.delText.visibility = View.GONE
            indexDel = 0
        }
    }
}