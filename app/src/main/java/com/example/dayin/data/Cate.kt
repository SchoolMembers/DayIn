package com.example.dayin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cate(
    @PrimaryKey(autoGenerate = true)
    val cateId: Long = 0L, //식별자
    var name: String, //카테고리 이름
)
