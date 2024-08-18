package com.schedule.dayin.data.mainD

import java.util.Date

data class AutoMoney(
    val inEx: Int,
    val title: String?,
    val money: Int
)

data class MoneyName(
    val money: Int,
    val name: String
)

data class timeData(
    val id: Long = 0L,
    var date: Date,
    var title: String = "@string/nTitle",
    var time: Int = 0
)
