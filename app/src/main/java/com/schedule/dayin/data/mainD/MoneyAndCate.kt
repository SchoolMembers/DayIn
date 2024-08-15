package com.schedule.dayin.data.mainD

import androidx.room.Embedded
import androidx.room.Relation
import com.schedule.dayin.fragments.ScheduleFragment

data class MoneyAndCate(
    @Embedded val moneyDb: MoneyDb,
    @Relation(
        parentColumn = "cateId",
        entityColumn = "cateId"
    )
    val cate: CateDb
)
