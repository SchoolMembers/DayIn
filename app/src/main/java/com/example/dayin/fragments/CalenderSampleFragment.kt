package com.example.dayin.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calender_sample_app.shared.displayText
import com.example.calender_sample_app.view.addStatusBarColorUpdate
import com.example.calender_sample_app.view.getColorCompat
import com.example.calender_sample_app.view.layoutInflater
import com.example.calender_sample_app.view.setTextColorRes
import com.example.dayin.R
import com.example.dayin.data.Calender
import com.example.dayin.data.dateTimeFormatter
import com.example.dayin.data.generateCalenders
import com.example.dayin.databinding.CalenderSampleBinding
import com.example.dayin.databinding.CalenderSampleCalendarHeaderBinding
import com.example.dayin.databinding.CalenderSampleCalenderDayBinding
import com.example.dayin.databinding.CalenderSampleCalenderItemViewBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalenderSampleCalendersAdapter :
    RecyclerView.Adapter<CalenderSampleCalendersAdapter.CalenderSampleCalendersViewHolder>() {
    val calenders = mutableListOf<Calender>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderSampleCalendersViewHolder {
        return CalenderSampleCalendersViewHolder(
            CalenderSampleCalenderItemViewBinding.inflate(parent.context.layoutInflater, parent, false),
        )
    }

    override fun onBindViewHolder(viewHolder: CalenderSampleCalendersViewHolder, position: Int) {
        viewHolder.bind(calenders[position])
    }

    override fun getItemCount(): Int = calenders.size

    inner class CalenderSampleCalendersViewHolder(val binding: CalenderSampleCalenderItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(calender: Calender) {
            binding.itemFlightDateText.apply {
                text = dateTimeFormatter.format(calender.time)
                setBackgroundColor(itemView.context.getColorCompat(calender.color))
            }

            binding.itemDepartureAirportCodeText.text = calender.departure.code
            binding.itemDepartureAirportCityText.text = calender.departure.city

            binding.itemDestinationAirportCodeText.text = calender.destination.code
            binding.itemDestinationAirportCityText.text = calender.destination.city
        }
    }
}


class CalenderSampleFragment : Fragment() {
    private lateinit var binding: CalenderSampleBinding
    private var selectedDate: LocalDate? = null

    private val calendersAdapter = CalenderSampleCalendersAdapter()

    private val calenders = generateCalenders().groupBy { it.time.toLocalDate() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.calender_sample, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addStatusBarColorUpdate(R.color.calender_sample_toolbar_color)
        binding = CalenderSampleBinding.bind(view)

        binding.calenderSampleDetail.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = calendersAdapter
        }
        calendersAdapter.notifyDataSetChanged()

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)

        configureBinders(daysOfWeek)

        binding.calenderSampleCalender.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calenderSampleCalender.scrollToMonth(currentMonth)

        binding.calenderSampleCalender.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calenderSampleCalender.notifyDateChanged(it)
//                updateAdapterForDate(null)
            }
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            binding.calenderSampleCalender.findFirstVisibleMonth()?.let {
                binding.calenderSampleCalender.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            binding.calenderSampleCalender.findFirstVisibleMonth()?.let {
                binding.calenderSampleCalender.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }
    }

    private fun updateAdapterForDate(date: LocalDate?) {
        calendersAdapter.calenders.clear()
        calendersAdapter.calenders.addAll(calenders[date].orEmpty())
        calendersAdapter.notifyDataSetChanged()
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalenderSampleCalenderDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@CalenderSampleFragment.binding
                            binding.calenderSampleCalender.notifyDateChanged(day.date)
                            oldDate?.let { binding.calenderSampleCalender.notifyDateChanged(it) }
                            updateAdapterForDate(day.date)
                        }
                    }
                }
            }
        }
        binding.calenderSampleCalender.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.calenderSampleDayText
                val layout = container.binding.exFiveDayLayout
                textView.text = data.date.dayOfMonth.toString()

                val flightTopView = container.binding.calenderSampleTop
                val flightBottomView = container.binding.calenderSampleBottom
                flightTopView.background = null
                flightBottomView.background = null

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColorRes(R.color.calender_sample_text_grey)
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.calender_sample_selected_bg else 0)

                    val calenders = calenders[data.date]
                    if (calenders != null) {
                        if (calenders.count() == 1) {
                            flightBottomView.setBackgroundColor(context.getColorCompat(calenders[0].color))
                        } else {
                            flightTopView.setBackgroundColor(context.getColorCompat(calenders[0].color))
                            flightBottomView.setBackgroundColor(context.getColorCompat(calenders[1].color))
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.calender_sample_text_grey_light)
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalenderSampleCalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.calenderSampleCalender.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColorRes(R.color.white)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }
}