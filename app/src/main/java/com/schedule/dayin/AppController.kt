package com.schedule.dayin

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.schedule.dayin.data.mainD.MainDatabase
import com.schedule.dayin.data.memoD.MemoDatabase

class AppController : Application() {
    val mainDb: MainDatabase by lazy { MainDatabase.getDatabase(this) }
    val memoDb: MemoDatabase by lazy { MemoDatabase.getDatabase(this) }
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "568f4f05dac3e439d6e3359440d566ef")
    }

}