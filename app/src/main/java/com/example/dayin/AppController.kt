package com.example.dayin

import android.app.Application
import com.example.dayin.data.mainD.MainDatabase
import com.example.dayin.data.memoD.MemoDatabase

class AppController : Application() {
    val mainDatabase: MainDatabase by lazy { MainDatabase.getDatabase(this) }
    val memoDatabase: MemoDatabase by lazy { MemoDatabase.getDatabase(this) }
}
