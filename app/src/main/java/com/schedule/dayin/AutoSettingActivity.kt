package com.schedule.dayin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.repository.MoneyRepository
import com.schedule.dayin.data.mainD.repository.ScheduleRepository
import com.schedule.dayin.databinding.AutoSettingBinding
import com.schedule.dayin.views.AutoSettingMoneyAdapter
import com.schedule.dayin.views.AutoSettingScheAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoSettingActivity: AppCompatActivity() {

    private lateinit var binding: AutoSettingBinding

    //데이터
    private lateinit var appController: AppController
    private lateinit var mainDb: MainDatabase
    private lateinit var moneyRepository: MoneyRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AutoSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // 하단 바 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.view) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        appController = this.application as AppController
        mainDb = appController.mainDb
        moneyRepository = MoneyRepository(mainDb.moneyDbDao())
        scheduleRepository = ScheduleRepository(mainDb.scheduleDbDao())

        binding.backButton.setOnClickListener {
            finish()
        }

        val autoScheWeek = binding.autoScheWeek
        val autoScheMon = binding.autoScheMon
        val autoScheYear = binding.autoScheYear

        val autoMoneyWeek = binding.autoMoneyWeek
        val autoMoneyMon = binding.autoMoneyMon
        val autoMoneyYear = binding.autoMoneyYear

        autoScheWeek.layoutManager = LinearLayoutManager(this)
        autoScheMon.layoutManager = LinearLayoutManager(this)
        autoScheYear.layoutManager = LinearLayoutManager(this)

        autoMoneyWeek.layoutManager = LinearLayoutManager(this)
        autoMoneyMon.layoutManager = LinearLayoutManager(this)
        autoMoneyYear.layoutManager = LinearLayoutManager(this)

        uiScope.launch {
            //일정 매주
            try {
                val autoList= mutableListOf<ScheduleDb>()

                var title = ""

                withContext(Dispatchers.IO) {
                    scheduleRepository.getAutoId(1).forEach {
                        if (title != it.title) {
                            title = it.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoScheWeek.adapter = AutoSettingScheAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoScheWeek.visibility = View.GONE
                    binding.noDataSche1.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoScheWeek.visibility = View.GONE
                binding.noDataSche1.visibility = View.VISIBLE
            }

            //일정 매달
            try {
                val autoList= mutableListOf<ScheduleDb>()

                var title = ""

                withContext(Dispatchers.IO) {
                    scheduleRepository.getAutoId(2).forEach {
                        if (title != it.title) {
                            title = it.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoScheMon.adapter = AutoSettingScheAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoScheMon.visibility = View.GONE
                    binding.noDataSche2.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoScheMon.visibility = View.GONE
                binding.noDataSche2.visibility = View.VISIBLE
            }

            //일정 매년
            try {
                val autoList= mutableListOf<ScheduleDb>()

                var title = ""

                withContext(Dispatchers.IO) {
                    scheduleRepository.getAutoId(3).forEach {
                        if (title != it.title) {
                            title = it.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoScheYear.adapter = AutoSettingScheAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoScheYear.visibility = View.GONE
                    binding.noDataSche3.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoScheYear.visibility = View.GONE
                binding.noDataSche3.visibility = View.VISIBLE
            }

            //가계부 매주
            try {
                val autoList= mutableListOf<MoneyAndCate>()

                var title: String? = ""

                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoIdMoney(1).forEach {
                        if (title != it.moneyDb.title) {
                            title = it.moneyDb.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoMoneyWeek.adapter = AutoSettingMoneyAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoMoneyWeek.visibility = View.GONE
                    binding.noDataMoney1.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoMoneyWeek.visibility = View.GONE
                binding.noDataMoney1.visibility = View.VISIBLE
            }

            //가계부 매달
            try {
                val autoList= mutableListOf<MoneyAndCate>()

                var title: String? = ""

                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoIdMoney(2).forEach {
                        if (title != it.moneyDb.title) {
                            title = it.moneyDb.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoMoneyMon.adapter = AutoSettingMoneyAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoMoneyMon.visibility = View.GONE
                    binding.noDataMoney2.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoMoneyMon.visibility = View.GONE
                binding.noDataMoney2.visibility = View.VISIBLE
            }

            //가계부 매년
            try {
                val autoList= mutableListOf<MoneyAndCate>()

                var title: String? = ""

                withContext(Dispatchers.IO) {
                    moneyRepository.getAutoIdMoney(3).forEach {
                        if (title != it.moneyDb.title) {
                            title = it.moneyDb.title
                            autoList.add(it)
                        }
                    }
                }

                if (autoList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        autoMoneyYear.adapter = AutoSettingMoneyAdapter(this@AutoSettingActivity, appController, autoList)
                    }
                } else {
                    binding.autoMoneyYear.visibility = View.GONE
                    binding.noDataMoney3.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // 데이터 없음
                binding.autoMoneyYear.visibility = View.GONE
                binding.noDataMoney3.visibility = View.VISIBLE
            }

        }

    }
}