package com.schedule.dayin.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.repository.CateRepository
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.databinding.MoneyRecyItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MoneyAdapter(private val context: Context,  private var dataList: MutableList<MoneyAndCate>, private val appController: AppController, private val day: CalendarDay, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mainDb = appController.mainDb
    private var moneyRepository = MoneyRepository(mainDb.moneyDbDao())
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val cateRepository = CateRepository(mainDb.cateDao())
    private var cateList: Flow<List<CateDb>> = emptyFlow()
    private lateinit var cateAdapter: CateAdapter
    private var selectedCate: CateDb? = null //선택된 카테고리
    private var cateId: Long = -1L
    private lateinit var cateRecyclerView: RecyclerView
    private var inEx = 0

    //사용자 지정 카테고리
    private lateinit var userCateRecyclerView: RecyclerView

    private lateinit var userCateAdapter: UserCateAdapter

    private var userCateList: Flow<List<MoneyAndCate>> = emptyFlow()

    private var userCateListBefore: Flow<List<CateDb>> = emptyFlow()

    //선택된 삭제할 카테고리
    private var delCates: List<CateDb>? = null
    private lateinit var delCheckButton: TextView


    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MoneyViewHolder(MoneyRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MoneyViewHolder).binding



        binding.text.text = dataList[position].moneyDb.money.toString()
        binding.category.text = dataList[position].cateDb.name

        if (dataList[position].cateDb.inEx == 0) {
            binding.text.setTextColor(ContextCompat.getColor(context, R.color.red))
            binding.prefix.setTextColor(ContextCompat.getColor(context, R.color.red))
            binding.prefix.text = "-"
        } else {
            binding.text.setTextColor(ContextCompat.getColor(context, R.color.green3))
            binding.prefix.setTextColor(ContextCompat.getColor(context, R.color.green3))
            binding.prefix.text = "+"
        }


        //아이템 클릭 리스너
        binding.layout.setOnClickListener {
            showItemDialog(dataList[position], position)
        }


    }

    //아이템 클릭 다이얼로그
    private fun showItemDialog(data: MoneyAndCate, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.item_dialog_m, null)
        val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
        val dialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.money).text = data.moneyDb.money.toString() + " 원"


        if (data.cateDb.inEx == 0){
            dialogView.findViewById<TextView>(R.id.mp).text = "지출"
        } else {
            dialogView.findViewById<TextView>(R.id.mp).text = "수익"
        }

        dialogView.findViewById<TextView>(R.id.cate).text = data.cateDb.name

        if (data.moneyDb.memo != null) {
            dialogView.findViewById<TextView>(R.id.memoText).text = data.moneyDb.memo
        }

        if (data.moneyDb.auto != 0) {
            dialogView.findViewById<TextView>(R.id.autoTitle).text = data.moneyDb.title
        }

        //닫기
        dialogView.findViewById<TextView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        //삭제
        dialogView.findViewById<TextView>(R.id.deleteButton).setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    moneyRepository.deleteMoney(data.moneyDb)
                }
                withContext(Dispatchers.Main) {
                    dataList.removeAt(position)
                    notifyDataSetChanged()
                    onDataChanged?.invoke()
                    dialog.dismiss()
                }

            }
        }

        //수정
        dialogView.findViewById<TextView>(R.id.editButton).setOnClickListener {
            dialog.dismiss()
            showEditDialog(data, day)
        }

        dialog.show()

    }

    //수정 다이얼로그
    private fun showEditDialog(data: MoneyAndCate, day: CalendarDay) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_m_add, null)
        val dialogBuilder = android.app.AlertDialog.Builder(context).setView(dialogView)
        val dialog = dialogBuilder.create()


        //date 설정 (데이터베이스 등록)
        val calendar = Calendar.getInstance()
        calendar.set(day.date.year, day.date.monthValue - 1, day.date.dayOfMonth) // 날짜 설정

        //체크 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.save_icon, null)

        //카테고리 리사이클러 뷰
        inEx = data.cateDb.inEx
        cateRecyclerView = dialogView.findViewById(R.id.cateRecyclerView)
        cateRecyclerView.layoutManager = GridLayoutManager(context, 3)

        //카테고리 설정
        cateId = data.moneyDb.cateId

        //닫기
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        //금액 입력
        val moneyTitle = dialogView.findViewById<EditText>(R.id.moneyText)

        var moneyText: String = data.moneyDb.money.toString()
        if (data.moneyDb.money > 0) {
            moneyTitle.setText(data.moneyDb.money.toString())
        }

        moneyTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                moneyText = s?.toString() ?: ""

                try {
                    if (moneyText == "" || moneyText.toLong() == 0L) {
                        checkButton.isEnabled = false
                        checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.false_check_icon, null)
                    } else {
                        checkButton.isEnabled = true
                        checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.save_icon, null)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "금액은 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                    moneyTitle.setText("")
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //소비 지출 토글
        val cateToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.cateToggle)

        if (inEx == 0) {
            cateToggle.check(R.id.minus)
        } else {
            cateToggle.check(R.id.plus)
        }

        //카테고리 로딩
        loadCate(inEx, cateRecyclerView, data.moneyDb.cateId)

        cateToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                inEx = when (checkedId) {
                    R.id.minus -> {
                        loadCate(0, cateRecyclerView, data.moneyDb.cateId)
                        0
                    }
                    R.id.plus -> {
                        loadCate(1, cateRecyclerView, data.moneyDb.cateId)
                        1
                    }
                    else -> 0
                }
            }
        }

        //카테고리 추가 버튼
        val catePlus = dialogView.findViewById<Button>(R.id.catePlus)
        catePlus.setOnClickListener {
            addCateDialog(data)
        }

        //카테고리 제거 버튼
        val cateDelete = dialogView.findViewById<Button>(R.id.cateMinus)
        cateDelete.setOnClickListener {
            deleteCateDialog(data)
        }

        //자동 등록
        var auto = data.moneyDb.auto
        val autoToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.autoToggle)

        val autoTitleLayout = dialogView.findViewById<LinearLayout>(R.id.autoTitleLayout)

        if (auto != 0) {
            autoTitleLayout.visibility = View.VISIBLE
        } else {
            autoTitleLayout.visibility = View.GONE
        }

        if (auto == 0) {
            autoToggle.check(R.id.autoDefault)
        } else if (auto == 1) {
            autoToggle.check(R.id.autoWeek)
        } else if (auto == 2) {
            autoToggle.check(R.id.autoMon)
        } else if (auto == 3) {
            autoToggle.check(R.id.autoYear)
        }

        autoToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                auto = when (checkedId) {
                    R.id.autoDefault -> 0
                    R.id.autoWeek -> 1
                    R.id.autoMon -> 2
                    R.id.autoYear -> 3
                    else -> auto
                }
                Log.d("customTag", "auto value updated: $auto")
                if (auto != 0) {
                    autoTitleLayout.visibility = View.VISIBLE
                } else {
                    autoTitleLayout.visibility = View.GONE
                }
            }
        }

        //자동 등록 제목
        val autoTitle = dialogView.findViewById<EditText>(R.id.autoTitle)
        autoTitle.setText(if (data.moneyDb.auto != 0) data.moneyDb.title else "")
        var autoText: String? = if (data.moneyDb.auto != 0) data.moneyDb.title else null
        autoTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                autoText = s?.toString() ?: if (data.moneyDb.auto != 0) data.moneyDb.title else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 자동 등록 info 리스너
        val infoButton1 = dialogView.findViewById<Button>(R.id.infoButton1)
        infoButton1.setOnClickListener {
            Toast.makeText(context, R.string.auto_money, Toast.LENGTH_SHORT).show()
        }

        //메모
        val memo = dialogView.findViewById<EditText>(R.id.memoText)
        memo.setText(if (data.moneyDb.memo != null) data.moneyDb.memo else "")
        var memoText: String? = if (data.moneyDb.memo != null) data.moneyDb.memo else ""
        memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memoText = s?.toString() ?: if (data.moneyDb.memo != null) data.moneyDb.memo else ""
                Log.d("customTag", "MoneyFragment onViewCreated called; memoText: $memoText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        //체크 버튼
        checkButton.setOnClickListener {
            if (autoText == "" && auto != 0) {
                Toast.makeText(context, "자동 등록 활성화 시 반드시 제목을 입력해야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (memoText == "") {
                memoText = null
            }
            if (auto == 0) {
                autoText = null
            }

            if (moneyText == "" || moneyText == "0") {
                Toast.makeText(context, "금액을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                moneyText.toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "금액은 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (moneyText.toLong() < 0) {
                Toast.makeText(context, "금액은 0보다 커야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cateId == -1L) {
                Toast.makeText(context, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //데이터베이스에 저장
            Log.d("customTag", "MoneyFragment onViewCreated called; checkButton clicked")
            dialog.dismiss()
            val moneyDate = calendar.time
            var money: MoneyDb
            var cate: CateDb
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    moneyRepository.updateMoney(
                        MoneyDb(
                            id = data.moneyDb.id,
                            date = moneyDate,
                            money = moneyText.toLong(),
                            auto = auto,
                            memo = memoText,
                            title = autoText,
                            cateId = cateId
                        )
                    )

                    money = moneyRepository.onlyMoney(data.moneyDb.id)
                    cate = cateRepository.getCateById(cateId)

                    cateId = -1
                }

                withContext(Dispatchers.Main) {
                    val index = dataList.indexOfFirst { it.moneyDb.id == data.moneyDb.id }

                    if (index != -1) {
                        dataList[index] = MoneyAndCate(money, cate)

                        dataList = dataList.sortedWith(compareBy<MoneyAndCate> { it.cateDb.inEx }
                            .thenBy { it.moneyDb.id }).toMutableList()

                        notifyDataSetChanged()
                    }


                    //캘린더 셀 콜백
                    onDataChanged?.invoke()

                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    //카테고리 추가 다이얼로그
    private fun addCateDialog(data: MoneyAndCate) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_add, null)
        val dialogBuilder = android.app.AlertDialog.Builder(context).setView(dialogView)
        val dialog = dialogBuilder.create()

        var addInEx = 0

        //체크 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.isEnabled = false
        checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.false_check_icon, null)

        //카테고리 이름 입력
        val cateName = dialogView.findViewById<TextView>(R.id.editText)
        var cateText: String = ""

        cateName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cateText = s?.toString() ?: ""
                Log.d("customTag", "MoneyFragment onViewCreated called; moneyText: $cateText")

                if (cateText == "") {
                    checkButton.isEnabled = false
                    checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.false_check_icon, null)
                } else {
                    checkButton.isEnabled = true
                    checkButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.save_icon, null)
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
                Log.d("customTag", "MoneyFragment onViewCreated called; cateToggle checked")
            }
        }

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        //체크 버튼 클릭
        checkButton.setOnClickListener {
            if (cateText == "") {
                Toast.makeText(context, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    cateRepository.insertCate(CateDb(name = cateText, inEx = addInEx))
                    loadCate(inEx, cateRecyclerView, data.moneyDb.cateId)
                }
            }

            dialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; cate add checkButton clicked")
        }

        dialog.show()
    }

    //카테고리 삭제 다이얼로그
    private fun deleteCateDialog(data: MoneyAndCate) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_del, null)
        val dialogBuilder = android.app.AlertDialog.Builder(context).setView(dialogView)
        val delDialog = dialogBuilder.create()

        //사용자 지정 리사이클러
        userCateRecyclerView = dialogView.findViewById(R.id.deleteRecyclerView)
        userCateRecyclerView.layoutManager = LinearLayoutManager(context)
        loadUserCate(userCateRecyclerView)

        //체크 버튼
        delCheckButton = dialogView.findViewById(R.id.checkButton)
        delCheckButton.isEnabled = false
        delCheckButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.false_check_icon, null)

        //체크 버튼 클릭
        delCheckButton.setOnClickListener {
            val selectedCategories = delCates
            if (selectedCategories.isNullOrEmpty()) {
                Toast.makeText(context, "삭제할 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                delReCheck(selectedCategories, delDialog, data)
            }
        }

        //닫기 버튼
        val closeButton = dialogView.findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            delDialog.dismiss()
            Log.d("customTag", "MoneyFragment onViewCreated called; dialog closed")
        }

        delDialog.show()
    }

    //삭제 다시 확인
    private fun delReCheck(selectedCategories: List<CateDb>, delDialog: android.app.AlertDialog, data: MoneyAndCate) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_c_del_re, null)
        val dialogBuilder = android.app.AlertDialog.Builder(context).setView(dialogView)
        val dialog = dialogBuilder.create()

        //텍스트 설정
        val textView = dialogView.findViewById<TextView>(R.id.cateList)
        textView.text = delCates?.joinToString(", ") { it.name }

        //확인 버튼
        val checkButton = dialogView.findViewById<TextView>(R.id.checkButton)
        checkButton.setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    selectedCategories.forEach { cate ->
                        moneyRepository.deleteMoneyByCateId(cate.cateId)
                    }
                    selectedCategories.forEach { cate ->
                        cateRepository.deleteCateById(cate.cateId)
                    }
                }
                withContext(Dispatchers.Main) {
                    loadCate(inEx, cateRecyclerView, data.moneyDb.cateId)
                    dialog.dismiss()
                    delCates = null

                    //카테고리 삭제 선택 다이얼로그도 종료
                    delDialog.dismiss()
                }
            }
        }

        //취소 버튼
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    //사용자 카테고리 데이터 로드
    private fun loadUserCate(userCateRecyclerView: RecyclerView) {
        uiScope.launch {
            userCateListBefore = cateRepository.getUserCate()
            userCateList = moneyRepository.getUserCate()
            userCateListBefore.collect { list ->
                if (list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        userCateAdapter = UserCateAdapter(context, userCateListBefore, userCateList, onDataChanged = { cateDel() })
                        userCateRecyclerView.adapter = userCateAdapter
                        Log.d("customTag", "MoneyFragment loadUserCate: Adapter set with ${list.size} items.")
                    }
                } else {
                    Log.d("customTag", "MoneyFragment loadUserCate: userCateList is empty.")
                }
            }
        }
    }

    //카테고리 삭제 리스트 등록 함수
    private fun cateDel() {
        delCates = userCateAdapter.getSelectedCategory()

        //체크 버튼 활성/비활성
        if (delCates == null) {
            delCheckButton.isEnabled = false
            delCheckButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.false_check_icon, null)
        } else {
            delCheckButton.isEnabled = true
            delCheckButton.background = ResourcesCompat.getDrawable(context.resources, R.drawable.save_icon, null)
        }
    }


    //카테고리 데이터 로드
    private fun loadCate(inEx: Int, cateRecyclerView: RecyclerView, choice: Long? = null) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                cateList = cateRepository.getCateByInEx(inEx)
                cateAdapter = CateAdapter(cateList, choice, onDataChanged = { onCateDataChanged() })
                cateRecyclerView.adapter = cateAdapter
                Log.d("customTag", "MoneyFragment onViewCreated called; cateList updated")
            }
        }
    }

    //카테고리 변경 함수
    private fun onCateDataChanged() {
        selectedCate = cateAdapter.getSelectedCategory()
        cateId = selectedCate?.cateId ?: -1L
    }
}