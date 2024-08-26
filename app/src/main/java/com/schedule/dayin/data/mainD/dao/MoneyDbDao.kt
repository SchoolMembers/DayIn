package com.schedule.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.ScheduleDb
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(moneyDb: MoneyDb)

    @Update
    suspend fun update(moneyDb: MoneyDb)

    @Delete
    suspend fun delete(moneyDb: MoneyDb)

    //카테고리가 일치하는 데이터들 삭제
    @Query("DELETE FROM moneyDb WHERE cateId = :cateId")
    suspend fun deleteMoneyByCateId(cateId: Long)

    //id값과 일치하는 데이터 모든 정보(특정 기록 세부사항 수정)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE id = :id")
    fun getItem(id: Long): Flow<MoneyAndCate>

    //id값과 일치하는 데이터 모든 정보(특정 기록 세부사항 수정)
    @Transaction
    @Query("SELECT * FROM moneyDb WHERE id = :id")
    fun getOnlyMoney(id: Long): MoneyDb

    //date값 기준 오름차순 정렬 모든 데이터(처음 달력에 세팅할 때)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId ORDER BY date, inEx, money ASC")
    fun getAllMoney(): Flow<List<MoneyAndCate>>

    //특정 날짜의 데이터들(날짜칸 클릭했을 때 세팅)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE  :startDate <= date and date <= :endDate ORDER BY inEx, id ASC")
    fun getMoneyDay(startDate: Long, endDate: Long): List<MoneyAndCate>

    //사용자 지정 카테고리 데이터들
    @Transaction
    @Query("SELECT * FROM catedb JOIN moneyDb ON moneyDb.cateId = cateDb.cateId WHERE cateDb.cateId >= 25 ORDER BY moneyDb.cateId ASC")
    fun getUserCate(): Flow<List<MoneyAndCate>>

    //자동 등록된 데이터들 (자동 모아보기에서 사용)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE auto <> 0 ORDER BY inEx, title, money ASC")
    fun getAutoMoney(): List<MoneyAndCate>

    //특정 월의 데이터 (소비패턴에서 사용)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE (inEx = :inEx) AND (strftime('%m', moneyDb.date) IN (:mon)) And (strftime('%Y', moneyDb.date) = :year)")
    fun getMoneyMonth(year: String, mon: String, inEx: Int): Flow<List<MoneyAndCate>>

    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE (:startDate <= date and date <= :endDate) AND inEx = :inEx")
    fun onlyMoneyMonth(startDate: Long, endDate: Long, inEx: Int): List<MoneyDb>

    //특정 년의 데이터 (소비패턴에서 사용)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE (inEx = :inEx) AND (strftime('%Y', moneyDb.date ) = :date)")
    fun getMoneyYear(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>>
}