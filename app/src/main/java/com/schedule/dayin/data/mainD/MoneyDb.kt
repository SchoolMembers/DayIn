package com.schedule.dayin.data.mainD

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class MoneyDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var date: Date, //등록 날짜
    var money: Long, //금액
    var auto: Int = 0, //자동 등록 여부 0:기본(등록안함) 1:매주 2:매달 3:매년
    var memo: String? = null, //메모
    var title: String? = null, //자동 등록 시 필수 이름(제목)

    var cateId: Long //카테고리 식별자
)