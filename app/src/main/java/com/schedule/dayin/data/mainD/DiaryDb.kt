package com.schedule.dayin.data.mainD

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class DiaryDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var date: Date, //등록 날짜
    var title: String = "", //제목
    var des: String? = null, //내용
    var feel: Int? = null, //기분
)