package com.example.dayin.data

import androidx.room.Embedded
import androidx.room.Relation

data class MoneyAndCate(
    @Embedded val moneyDb: MoneyDb,
    @Relation(
        parentColumn = "cateId",
        entityColumn = "cateId"
    )
    val cate: Cate
)
