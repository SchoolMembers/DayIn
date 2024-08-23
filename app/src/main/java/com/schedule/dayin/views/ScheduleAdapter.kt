package com.schedule.dayin.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendar.core.CalendarDay
import com.schedule.dayin.AppController
import com.schedule.dayin.R
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.ScheduleRepository
import com.schedule.dayin.databinding.ScheduleRecyItemsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleAdapter(private val context: Context, private var dataList: MutableList<ScheduleDb>, private val clickCheck: Boolean, private val appController: AppController, private val day: CalendarDay, private val onDataChanged: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("pref", Activity.MODE_PRIVATE)
    }

    private var mainDb = appController.mainDb
    private  var scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())
    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun updateData(newData: MutableList<ScheduleDb>) {
        dataList = newData
        notifyDataSetChanged()
    }

    fun formatDate(date: Date?): String {
        return date?.let {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
            dateFormat.format(it)
        } ?: "" // null인 경우 빈 문자열 반환
    }

    fun formatDateKo(date: Date?): String {
        return date?.let {
            val dateFormat = SimpleDateFormat("HH시 mm분", Locale.KOREAN)
            dateFormat.format(it)
        } ?: "" // null인 경우 빈 문자열 반환
    }

    // SharedPreferences Editor 객체
    private val editor: SharedPreferences.Editor by lazy {
        sharedPreferences.edit()
    }

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ScheduleViewHolder(ScheduleRecyItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ScheduleViewHolder).binding



        binding.text.text = dataList[position].title

        // 체크박스 초기 상태 설정
        val id = dataList[position].id
        val isChecked = loadCheck(id) == 1
        binding.check.isChecked = isChecked

        //시간 포매팅
        val time: String
        if (dataList[position].time != 0) {
            time = formatDate(dataList[position].date)
        } else {
            time = ""
        }

        //레이아웃 디자인 변경
        if (dataList[position].time == 0) {
            binding.time.visibility = ViewGroup.INVISIBLE
        } else {
            binding.time.text = time
            binding.time.visibility = ViewGroup.VISIBLE
        }

        //색상 변경
        //colorDefault(lightGray) : 0 | colorGray : 1 | colorYellow : 2 | colorPurple : 3 | colorBlue : 4 | colorGreen 5
        when (dataList[position].color) {
            0 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_light_gray)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            1 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_dark_gray)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.time.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            2 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_yellow)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            3 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_purple)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            4 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_blue)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            5 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_green)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            6 -> {
                binding.layout.background = ContextCompat.getDrawable(context, R.drawable.recy_items_back_red)
                binding.text.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }




        //dp 단위 px로 변환
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }



        val layoutParams = binding.layout.layoutParams as ViewGroup.MarginLayoutParams

        //날짜 칸 누른 상태일 때
        if (clickCheck) {
            binding.text.maxLines = 10

            //레이아웃 클릭
            binding.layout.isClickable = true
            binding.text.isClickable = true
            binding.time.isClickable = true

            //아이템 터치 리스너
            binding.text.setOnClickListener {

                Log.d("customTag", "ScheduleAdapter onBindViewHolder called; item clicked")

                showItemDialog(dataList[position], position)

            }

            if (dataList[position].time == 0) {
                binding.time.visibility = ViewGroup.INVISIBLE
            } else {
                binding.time.visibility = ViewGroup.VISIBLE
            }

            binding.check.visibility = ViewGroup.VISIBLE


            val marginInPx = dpToPx(5)
            layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            binding.layout.layoutParams = layoutParams
        }
        //달력 상태일 때
        else {
            binding.text.maxLines = 1

            binding.layout.isClickable = false
            binding.text.isClickable = false

            binding.check.visibility = ViewGroup.GONE
            binding.time.visibility = ViewGroup.GONE
            val marginInPx = dpToPx(0)
            layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            binding.layout.layoutParams = layoutParams
        }



        //체크리스트 클릭
        binding.check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                saveCheck(id, true)
            } else {
                saveCheck(id, false)
            }
        }

    }
    // 체크 상태 저장
    private fun saveCheck(id: Long, check: Boolean) {
        val idString = id.toString()
        val result = if (check) 1 else 0
        editor.putInt(idString, result)
        editor.commit()
    }

    // 체크 상태 로드
    private fun loadCheck(id: Long): Int {
        val idString = id.toString()
        return sharedPreferences.getInt(idString, 0)
    }

    //자세히 보기 다이얼로그
    private fun showItemDialog(data: ScheduleDb, position: Int) {

        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.item_dialog_s, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        dialogView.findViewById<TextView>(R.id.title).text = data.title

        if (data.memo != null) {
            dialogView.findViewById<TextView>(R.id.memoText).text = data.memo
        }

        dialogView.findViewById<TextView>(R.id.time).text = when (data.time) {
            1 -> formatDateKo(data.date)
            else -> "없음"
        }

        dialogView.findViewById<TextView>(R.id.notify).text = when (data.notify) {
            1 -> "하루 전"
            2 -> "1시간 전"
            3 -> "30분 전"
            else -> "없음"
        }

        dialogView.findViewById<TextView>(R.id.auto).text = when (data.auto) {
            1 -> "매주"
            2 -> "매달"
            3 -> "매년"
            else -> "없음"
        }

        //수정
        dialogView.findViewById<Button>(R.id.editButton).setOnClickListener {
            dialog.dismiss()
            showEditDialog(data, day)
        }

        //삭제
        dialogView.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    scheduleRepository.deleteSche(data)
                }
                withContext(Dispatchers.Main) {

                    dataList.removeAt(position)

                    notifyItemRemoved(position)

                    //캘린더 셀 콜백
                    onDataChanged?.invoke()

                    dialog.dismiss()
                }
            }
        }

        //닫기
        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        // 다이얼로그 표시
        dialog.show()

    }

    //수정 다이얼로그
    private fun showEditDialog(data: ScheduleDb, day: CalendarDay) {

        Log.d("customTag", "ScheduleAdapter onViewCreated called; editDialog")

        // 다이얼로그 빌더를 사용해 다이얼로그 생성
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_s_add, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        //날짜 설정
        setupDialog(dialogView, day)

        //date 설정
        val calendar = Calendar.getInstance()
        calendar.set(day.date.year, day.date.monthValue - 1, day.date.dayOfMonth) // 날짜 설정

        // 닫기 버튼 클릭 시 다이얼로그 닫기
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleAdapter onViewCreated called; dialog closed")
        }

        //제목
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleText)
        titleEditText.setText(data.title)

        var titleText: String = data.title

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                titleText = s?.toString() ?: "제목 없음"
                Log.d("customTag", "ScheduleFragment onViewCreated called; titleText: $titleText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // autoToggle 리스너
        var auto = when (data.auto) {
            1 -> 1
            2 -> 2
            3 -> 3
            else -> 0
        }

        val autoToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.autoToggle)
        when (data.auto) {
            1 -> autoToggle.check(R.id.autoWeek)
            2 -> autoToggle.check(R.id.autoMon)
            3 -> autoToggle.check(R.id.autoYear)
            else -> autoToggle.check(R.id.autoDefault)
        }

        autoToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                auto = when (checkedId) {
                    R.id.autoDefault -> 0
                    R.id.autoWeek -> 1
                    R.id.autoMon -> 2
                    R.id.autoYear -> 3
                    else -> auto // 기본값 유지
                }
                Log.d("customTag", "auto value updated: $auto")
            }
        }

        // autoToggle info 리스너
        val infoButton1 = dialogView.findViewById<Button>(R.id.infoButton1)
        infoButton1.setOnClickListener {
            Toast.makeText(context, R.string.auto_info1, Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; infoButton1 clicked")
        }

        //시간 등록 활성화 버튼
        val timeSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.timeSwitch)

        val timeHourEditText = dialogView.findViewById<EditText>(R.id.timeHour)
        val timeMinEditText = dialogView.findViewById<EditText>(R.id.timeMin)

        //시간 등록 활성화 이벤트
        val timeLayout = dialogView.findViewById<View>(R.id.timeLayout)

        // 초기화 플래그
        var isInitializing = true


        val textWatcher: TextWatcher? = null

        var timeHourText = 0
        var timeMinText = 0

        val errorMessage = context.getString(R.string.value_error)

        var noti = when (data.notify) {
            1 -> 1
            2 -> 2
            3 -> 3
            else -> 0
        }

        var time = when (data.time) {
            1 -> 1
            else -> 0
        }

        //시간 등록 활성화 -> 토글 활성화
        fun initializeTimeFields() {
            Log.d("customTag", "ScheduleFragment onViewCreated called; initializeTimeFields")
            timeSwitch.isChecked = time == 1
            timeLayout.visibility = if (time == 1) View.VISIBLE else View.GONE

            if (data.time == 1){
                timeHourEditText.setText(SimpleDateFormat("HH", Locale.KOREA).format(data.date))
                timeMinEditText.setText(SimpleDateFormat("mm", Locale.KOREA).format(data.date))
            }

            val hourText = timeHourEditText.text.toString()
            val minText = timeMinEditText.text.toString()

            if (data.time == 1) {
                timeHourText = hourText.toIntOrNull() ?: 0
                timeMinText = minText.toIntOrNull() ?: 0

                calendar.set(Calendar.HOUR_OF_DAY, timeHourText)
                calendar.set(Calendar.MINUTE, timeMinText)
                calendar.set(Calendar.SECOND, 0)
            }

            if (data.time == 0) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
            }

            isInitializing = false
        }



        val notifyToggle = dialogView.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.notify)


        fun updateTextField(editText: EditText, value: Int, max: Int, errorMessage: String) {
            if (value >= max) {
                try {
                    editText.removeTextChangedListener(textWatcher) // 무한 루프 방지
                    editText.setText("")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }  catch(e: Exception) {
                    editText.addTextChangedListener(textWatcher) // 리스너 재등록
                }
            }
        }


        // 시간 EditText에 대한 공통 TextWatcher 생성 함수
        fun createTextWatcher(isHourEditText: Boolean) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hourText = timeHourEditText.text.toString()
                val minText = timeMinEditText.text.toString()

                if (data.time == 1) {
                    timeHourText = hourText.toIntOrNull() ?: 0
                    timeMinText = minText.toIntOrNull() ?: 0
                }

                if (!isInitializing) {
                    val value = s?.toString()?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0

                    if (isHourEditText) {
                        timeHourText =  when {
                            value == 24 -> 0
                            else -> value
                        }
                        updateTextField(timeHourEditText, timeHourText, 25, errorMessage)
                    } else {
                        timeMinText = value
                        updateTextField(timeMinEditText, timeMinText, 60, errorMessage)
                    }
                }

                Log.d("customTag", "timeHourText updated: $timeHourText | timeMinText updated: $timeMinText")
                if (timeSwitch.isChecked) {
                    calendar.set(Calendar.HOUR_OF_DAY, timeHourText)
                    calendar.set(Calendar.MINUTE, timeMinText)
                    calendar.set(Calendar.SECOND, 0)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }


        //시간 활성화 스위치 리스너
        timeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){

                timeLayout.visibility = View.VISIBLE
                time = 1

                initializeTimeFields()


            }
            else {
                timeLayout.visibility = View.GONE
                if (data.time == 1) {
                    val hourText = timeHourEditText.text.toString()
                    val minText = timeMinEditText.text.toString()
                    timeHourText = hourText.toIntOrNull() ?: 0
                    timeMinText = minText.toIntOrNull() ?: 0
                } else {
                    timeHourText = 0
                    timeMinText = 0
                }

                isInitializing = true
                noti = 0
                time = 0
                Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch unchecked")
            }

        }

        Log.d("customTag", "ScheduleFragment onViewCreated called; timeSwitch checked")

        //시간 설정------------------------------------------------------------------------------------------

        initializeTimeFields()
        timeHourEditText.addTextChangedListener(createTextWatcher(isHourEditText = true))
        timeMinEditText.addTextChangedListener(createTextWatcher(isHourEditText = false))

        //알림 설정------------------------------------------------------------------------------------------
        when (noti) {
            1 -> notifyToggle.check(R.id.notiDay)
            2 -> notifyToggle.check(R.id.notiHour)
            3 -> notifyToggle.check(R.id.notiMin)
            else -> notifyToggle.check(R.id.notiDefault)
        }

        //알림 설정 리스너
        notifyToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                noti = when (checkedId) {
                    R.id.notiDefault -> 0
                    R.id.notiDay -> 1
                    R.id.notiHour -> 2
                    R.id.notiMin -> 3
                    else -> noti // 기본값 유지
                }
                Log.d("customTag", "notify value updated: $noti")
            }
        }

        // 알림 설정 info 리스너
        val infoButton2 = dialogView.findViewById<Button>(R.id.infoButton2)
        infoButton2.setOnClickListener {
            Toast.makeText(context, R.string.auto_info2, Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; infoButton2 clicked")
        }

        //색상 설정 리스너
        //colorDefault(lightGray) : 0 | colorGray : 1 | colorYellow : 2 | colorPurple : 3 | colorBlue : 4 | colorGreen 5
        var color = data.color
        val colorButton0 = dialogView.findViewById<Button>(R.id.colorDefault)
        colorButton0.setOnClickListener {
            color = 0
            Toast.makeText(context, "회색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated lightGray")
        }

        val colorButton1 = dialogView.findViewById<Button>(R.id.colorGray)
        colorButton1.setOnClickListener {
            color = 1
            Toast.makeText(context, "검정색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated gray")
        }

        val colorButton2 = dialogView.findViewById<Button>(R.id.colorYellow)
        colorButton2.setOnClickListener {
            color = 2
            Toast.makeText(context, "노란색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated yellow")
        }

        val colorButton3 = dialogView.findViewById<Button>(R.id.colorPurple)
        colorButton3.setOnClickListener {
            color = 3
            Toast.makeText(context, "보라색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated purple")
        }

        val colorButton4 = dialogView.findViewById<Button>(R.id.colorBlue)
        colorButton4.setOnClickListener {
            color = 4
            Toast.makeText(context, "파란색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated blue")
        }

        val colorButton5 = dialogView.findViewById<Button>(R.id.colorGreen)
        colorButton5.setOnClickListener {
            color = 5
            Toast.makeText(context, "초록색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated green")
        }

        val colorButton6 = dialogView.findViewById<Button>(R.id.colorRed)
        colorButton6.setOnClickListener {
            color = 6
            Toast.makeText(context, "빨간색으로 설정되었습니다", Toast.LENGTH_SHORT).show()
            Log.d("customTag", "ScheduleFragment onViewCreated called; color value updated red")
        }

        //메모
        val memo = dialogView.findViewById<EditText>(R.id.memoText)
        if (data.memo != null) {
            memo.setText(data.memo)
        }
        var memoText: String? = ""
        memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                memoText = s?.toString() ?: ""
                Log.d("customTag", "ScheduleFragment onViewCreated called; memoText: $memoText")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 체크 버튼
        val checkButton = dialogView.findViewById<Button>(R.id.checkButton)
        checkButton.setOnClickListener {
            dialog.dismiss()
            Log.d("customTag", "ScheduleFragment onViewCreated called; check button clicked")
            val scheduleDate = calendar.time


            //데이터 저장(일정 추가)
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    if (memoText == "") {
                        memoText = null
                    }

                    scheduleRepository.updateSche(
                        ScheduleDb(
                            id = data.id,
                            date = scheduleDate,
                            title = titleText,
                            auto = auto,
                            notify = noti,
                            memo = memoText,
                            check = loadCheck(data.id),
                            time = time,
                            color = color
                        )
                    )
                }
                withContext(Dispatchers.Main) {

                    val index = dataList.indexOfFirst { it.id == data.id }

                    if (index != -1) {
                        dataList[index] = ScheduleDb(
                            id = data.id,
                            date = scheduleDate,
                            title = titleText,
                            auto = auto,
                            notify = noti,
                            memo = memoText,
                            check = loadCheck(data.id),
                            time = time,
                            color = color
                        )
                        dataList = dataList.sortedWith(compareBy<ScheduleDb> { it.time }
                            .thenBy { it.date }
                            .thenBy { it.id }).toMutableList()
                        notifyDataSetChanged()
                    }

                    //캘린더 셀 콜백
                    onDataChanged?.invoke()

                    dialog.dismiss()
                }
            }

            Log.d("customTag", "ScheduleFragment onViewCreated called; data saved")
        }

        // 다이얼로그 표시
        dialog.show()
    }

    // 다이얼로그 내의 뷰들을 참조해 날짜 정보 설정
    private fun setupDialog(dialogView: View, day: CalendarDay) {
        val monYearTextView = dialogView.findViewById<TextView>(R.id.monYear)
        monYearTextView.text = day.date.format(DateTimeFormatter.ofPattern("MM월 dd일 (E)").withLocale(Locale.KOREAN))
        Log.d("customTag", "ScheduleAdapter onViewCreated called; monYearTextView updated")
    }
}