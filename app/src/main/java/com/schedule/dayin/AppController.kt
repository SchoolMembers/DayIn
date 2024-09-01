package com.schedule.dayin

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.memoD.MemoDatabase

class AppController : Application() {
    val mainDb: MainDatabase by lazy { MainDatabase.getDatabase(this) }
    val memoDb: MemoDatabase by lazy { MemoDatabase.getDatabase(this) }

    fun getEmojiFromIndex(index: Int?): String {
        return if (index != null && index in emojiArray.indices) {
            emojiArray[index]
        } else {
            ""
        }
    }

    // 이모티콘 배열 정의
    private val emojiArray = arrayOf(
        "\ud83d\ude0a", // 😊
        "\uD83D\uDE04", // 😄
        "\uD83D\uDE0D", // 😍
        "\ud83d\ude41", // 🙁
        "\uD83D\uDE22", // 😢
        "\uD83D\uDE21" // 😡
    )
}