package com.example.dayin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dayin.data.Type.Converters
import com.example.dayin.data.dao.CateDbDao
import com.example.dayin.data.dao.DiaryDbDao
import com.example.dayin.data.dao.MoneyDbDao
import com.example.dayin.data.dao.ScheduleDbDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScheduleDb::class, MoneyDb::class, CateDb::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDbDao(): ScheduleDbDao
    abstract fun moneyDbDao(): MoneyDbDao
    abstract fun cateDao(): CateDbDao
    abstract fun diaryDbDao(): DiaryDbDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(DatabaseCallback(context)).build().also { INSTANCE = it }
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.cateDao())
                }
            }
        }

        private suspend fun populateInitialData(cateDao: CateDbDao) {
            // 기본 카테고리
            val categories = listOf(
                //지출
                CateDb(name = "기타", inEx = 0),
                CateDb(name = "공과금", inEx = 0),
                CateDb(name = "주거", inEx = 0),
                CateDb(name = "여행", inEx = 0),
                CateDb(name = "의류", inEx = 0),
                CateDb(name = "경조사/선물", inEx = 0),
                CateDb(name = "음주", inEx = 0),
                CateDb(name = "카페/음료", inEx = 0),
                CateDb(name = "운동", inEx = 0),
                CateDb(name = "미용", inEx = 0),
                CateDb(name = "통신비", inEx = 0),
                CateDb(name = "구독비", inEx = 0),
                CateDb(name = "식비", inEx = 0),
                CateDb(name = "교통/차량", inEx = 0),
                CateDb(name = "보험료", inEx = 0),
                CateDb(name = "의료", inEx = 0),
                CateDb(name = "문화/취미", inEx = 0),
                CateDb(name = "마트/생필품", inEx = 0),
                CateDb(name = "교육", inEx = 0),

                //수입
                CateDb(name = "기타", inEx = 1),
                CateDb(name = "월급", inEx = 1),
                CateDb(name = "용돈", inEx = 1),
                CateDb(name = "부수입", inEx = 1),
                CateDb(name = "보너스", inEx = 1)

            )
            categories.forEach { cateDao.insert(it) }
        }
    }
}

