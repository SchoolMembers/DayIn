package com.example.dayin.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class MoneyDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var date: Date, //등록 날짜
    var money: Int, //금액
    var inEx: Int, //수입/지출 0:지출 1:수입
    var auto: Int = 0, //자동 등록 여부 0:기본(등록안함) 1:매주 2:매달 3:매년
    var memo: String?, //메모
    var pic: Bitmap?, //사진

    var cateId: Long //카테고리 식별자
)