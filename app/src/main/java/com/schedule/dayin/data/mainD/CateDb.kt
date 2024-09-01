package com.schedule.dayin.data.mainD

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CateDb(
    @PrimaryKey(autoGenerate = true)
    val cateId: Long = 0L, //식별자
    var name: String, //카테고리 이름
    var inEx: Int //수입/지출 0:지출 1:수입
)
