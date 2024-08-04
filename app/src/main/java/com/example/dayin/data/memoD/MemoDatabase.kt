package com.example.dayin.data.memoD

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.example.dayin.data.memoD.dao.MemoDbDao
import androidx.room.RoomDatabase

@Database(entities = [MemoDb::class], version = 1, exportSchema = false)
abstract class MemoDatabase: RoomDatabase() {
    abstract fun memoDbDao(): MemoDbDao

    companion object{
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}