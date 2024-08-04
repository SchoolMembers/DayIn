package com.example.dayin.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class DiaryDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var date: Date, //등록 날짜
    var title: String = "@string/nTitle", //제목
    var des: String?, //내용
    var pic: Bitmap?, //사진
    var feel: Int?, //기분
)