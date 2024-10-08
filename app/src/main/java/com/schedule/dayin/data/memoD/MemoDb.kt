package com.schedule.dayin.data.memoD

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MemoDb (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var title: String = "", //제목
    var des: String = "" //내용
)