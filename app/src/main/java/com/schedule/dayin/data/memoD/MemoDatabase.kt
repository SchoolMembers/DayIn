package com.schedule.dayin.data.memoD

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.schedule.dayin.data.memoD.dao.MemoDbDao
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.schedule.dayin.data.Converters

@Database(entities = [MemoDb::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MemoDatabase: RoomDatabase() {
    abstract fun memoDbDao(): MemoDbDao

    companion object {
        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getDatabase(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}