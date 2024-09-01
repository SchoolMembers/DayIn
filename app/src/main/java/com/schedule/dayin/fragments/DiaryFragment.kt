package com.schedule.dayin.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import com.schedule.dayin.R
import com.schedule.dayin.databinding.FragmentDBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.DaySize
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.schedule.dayin.MainActivity

class DiaryFragment : Fragment() {

    private var _binding: FragmentDBinding? = null
    private val binding get() = _binding!!


    //프래그먼트 뷰를 생성하고 초기화. 프래그먼트의 레이아웃 인플레이트 -> 뷰 반환
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDBinding.inflate(inflater, container, false)
        Log.d("customTag", "DiaryFragment onCreateView called")
        return binding.root
    }

    //onCreateView() 완료 후 호출. 프래그먼트 뷰 생성 이후 추가적인 초기화 작업. (뷰 관련 로직 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //애니메이션 비활성화
        with(binding.calendarView) {
            itemAnimator = null
        }

        //연, 월 표시 공간
        val barDateYear = (activity as? MainActivity)?.findViewById<TextView>(R.id.barDateYear)

        //kizitonwose calendar
        val calendarView = binding.calendarView
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(100)
        val lastMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek


        //시작 월, 종료 월, 첫 주의 요일
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.daySize = DaySize.Rectangle

        Log.d("customTag", "DiaryFragment onViewCreated called; day setup complete")


        //캘린더의 각 날짜 뷰 정의
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString() //dayText에 날짜 표시
                if (day.position == DayPosition.MonthDate) { //현재 날짜가 현재 월 내에 있을 때
                    container.textView.setTextColor(Color.BLACK)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }

            }
        }

        //스크롤 리스너 ( 월 스크롤 -> 연, 월 업데이트)
        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                barDateYear?.text = p1.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
                Log.d("customTag", "DiaryFragment onViewCreated called; barDateYear updated")
            }
        }

        // barDateYear 클릭 리스너 설정
        barDateYear?.setOnClickListener {
            showYearMonthPicker(calendarView, currentMonth)
        }

    }

    // 연도 및 월 선택 다이얼로그 보여주기
    private fun showYearMonthPicker(calendarView: com.kizitonwose.calendar.view.CalendarView, currentMonth: YearMonth) {
        // 다이얼로그 뷰 생성
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_month_picker, null)
        val yearEdit: EditText = dialogView.findViewById(R.id.year)
        val monthEdit: EditText = dialogView.findViewById(R.id.month)
        val confirmButton: Button = dialogView.findViewById(R.id.confirm_button)

        // 최소, 최대 날짜 계산
        val minYearMonth = currentMonth.minusMonths(98)
        val maxYearMonth = currentMonth.plusMonths(99)

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        //년
        var yearText: String = "0"
        yearEdit.hint = currentMonth.year.toString()
        yearEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                yearText = s?.toString() ?: "0"

            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //월
        var monText: String = "0"
        monthEdit.hint = currentMonth.monthValue.toString()
        monthEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                monText = s?.toString() ?: "0"

            }
            override fun afterTextChanged(s: Editable?) {}
        })

        confirmButton.setOnClickListener {
            //int 전환
            val selectedYear = if (yearText == "") 0 else yearText.toInt()
            val selectedMonth = if (monText == "") 0 else monText.toInt()

            //아무 값도 입력하지 않았을 때
            if (selectedYear == 0 || selectedMonth == 0) {
                Toast.makeText(requireContext(), "연도와 월을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //12 초과
            if (selectedMonth > 12) {
                Toast.makeText(requireContext(), "월은 12월 이하여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 등록할 날짜 세팅
            val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)

            Log.d("customTag", "Selected Year: $selectedYear, Selected Month: $selectedMonth")
            Log.d("customTag", "maxMon: $maxYearMonth, minMon: $minYearMonth")


            // 범위 초과 또는 미달 시 토스트 메시지 표시
            if ((selectedYear <= minYearMonth.year && selectedMonth < minYearMonth.monthValue)  || (selectedYear >= maxYearMonth.year && selectedMonth > maxYearMonth.monthValue)) {
                Toast.makeText(
                    requireContext(),
                    "선택할 수 있는 날짜 범위는 ${minYearMonth.year}년 ${minYearMonth.monthValue}월 ~ ${maxYearMonth.year}년 ${maxYearMonth.monthValue}월입니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // 선택한 연도와 월로 캘린더 스크롤
                calendarView.scrollToMonth(selectedYearMonth)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    //프래그먼트의 뷰가 파괴될 때 호출. 리소스 해제 등
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지를 위한 바인딩 해제
    }

    //정적 멤버
    companion object {
        fun newInstance(): DiaryFragment {
            val args = Bundle()
            val fragment = DiaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
    }
}