package com.example.dayin.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dayin.data.MoneyAndCate
import com.example.dayin.data.MoneyDb
import kotlinx.coroutines.flow.Flow

interface MoneyDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(moneyDb: MoneyDb)

    @Update
    suspend fun update(moneyDb: MoneyDb)

    @Delete
    suspend fun delete(moneyDb: MoneyDb)

    @Transaction
    @Query("SELECT * FROM moneyDb WHERE id = :id") //id값과 일치하는 데이터 모든 정보
    fun getItem(id: Long): Flow<MoneyAndCate>

    @Transaction
    @Query("SELECT * FROM moneyDb ORDER BY date") //date값 기준 오름차순 정렬 모든 데이터
    fun getAllItems(): Flow<List<MoneyAndCate>>

    @Transaction
    @Query("SELECT * FROM moneyDb WHERE date(date) = date(:date) ORDER BY inEx, money ASC") //특정 날짜의 데이터들
    fun getDay(date: String): Flow<List<MoneyAndCate>>

    @Transaction
    @Query("SELECT * FROM moneyDb WHERE auto <> 0 ORDER BY money ASC") //자동 등록된 데이터들 (자동 모아보기에서 사용)
    fun getAutoItems(): Flow<List<MoneyAndCate>>

    @Transaction
    @Query("SELECT money, strftime('%Y-%m', date) AS month FROM moneyDb WHERE inEx = :inEx ORDER BY date ASC") //수입 또는 지출; 금액, 날짜 데이터들 (소비패턴에서 사용)
    fun getIncome(inEx: Int): Flow<List<MoneyDb>>

    @Transaction
    @Query("SELECT moneyDb.money, moneyDb.inEx, cate.name FROM moneyDb JOIN cate ON moneyDb.cateId = cate.cateId WHERE strftime('%m', moneyDb.date) IN (:date)") //특정 월의 데이터 (소비패턴에서 사용)
    fun getMonth(date: List<String>): Flow<List<MoneyAndCate>>

    @Transaction
    @Query("SELECT moneyDb.money, moneyDb.inEx, cate.name FROM moneyDb JOIN cate ON moneyDb.cateId = cate.cateId WHERE strftime('%Y', moneyDb.date ) = :date") //특정 년의 데이터 (소비패턴에서 사용)
    fun getYear(date: List<String>): Flow<List<MoneyAndCate>>
}