package com.example.dayin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dayin.data.Type.Converters
import com.example.dayin.data.dao.MoneyDbDao
import com.example.dayin.data.dao.ScheduleDbDao

@Database(entities = [ScheduleDb::class, MoneyDb::class, Cate::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDbDao(): ScheduleDbDao
    abstract fun moneyDbDao(): MoneyDbDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also{ Instance = it }
            }
        }
    }
}