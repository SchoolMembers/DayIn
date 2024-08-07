package com.example.dayin

import android.app.Application
import com.example.dayin.data.mainD.MainDatabase
import com.example.dayin.data.memoD.MemoDatabase

class AppController : Application() {
    val mainDb: MainDatabase by lazy { MainDatabase.getDatabase(this) }
    val memoDb: MemoDatabase by lazy { MemoDatabase.getDatabase(this) }
}