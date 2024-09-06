package com.schedule.dayin.data.mainD

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class ScheduleDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, //식별자
    var date: Date, //등록 날짜
    var title: String = "@string/nTitle", //제목
    var auto: Int = 0, //자동 등록 여부 0:기본(등록안함) 1:매주 2:매달 3:매년
    var memo: String? = null, //메모
    var check: Int = 0, //체크리스트 완료 여부 0:미완료 1:완료
    var time: Int = 0, //시간 등록 여부 0:등록 안함 1:등록함
    var color: Int = 0 //레이아웃 색상
    /*var frd_pub: Int = 0, //친구 공개 여부 0:비공개 1:공개
    var frd_id: Int //공동 일정 등록 상대 유저 아이디*/
)

//즐겨찾기 데이터 저장 방법 추후 고민