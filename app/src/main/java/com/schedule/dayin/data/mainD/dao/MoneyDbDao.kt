package com.schedule.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.schedule.dayin.data.mainD.AutoMoney
import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.MoneyName
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(moneyDb: MoneyDb)

    @Update
    suspend fun update(moneyDb: MoneyDb)

    @Delete
    suspend fun delete(moneyDb: MoneyDb)

    //id값과 일치하는 데이터 모든 정보(특정 기록 세부사항 수정)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE id = :id")
    fun getItem(id: Long): Flow<MoneyAndCate>

    //date값 기준 오름차순 정렬 모든 데이터(처음 달력에 세팅할 때)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId ORDER BY date, inEx, money ASC")
    fun getAllMoney(): Flow<List<MoneyAndCate>>

    //특정 날짜의 데이터들(날짜칸 클릭했을 때 세팅)
    @Transaction
    @Query("SELECT * FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE date(date) = date(:date) ORDER BY inEx, money ASC")
    fun getMoneyDay(date: String): Flow<List<MoneyAndCate>>

    //자동 등록된 데이터들 (자동 모아보기에서 사용)
    @Transaction
    @Query("SELECT inEx, title, money FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE auto <> 0 ORDER BY inEx, title, money ASC")
    fun getAutoMoney(): Flow<List<AutoMoney>>

    //특정 월의 데이터 (소비패턴에서 사용)
    @Transaction
    @Query("SELECT moneyDb.money, cateDb.name FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE (inEx = :inEx) AND (strftime('%m', moneyDb.date) IN (:date))")
    fun getMoneyMonth(date: List<String>, inEx: Int): Flow<List<MoneyName>>

    //특정 년의 데이터 (소비패턴에서 사용)
    @Transaction
    @Query("SELECT moneyDb.money, cateDb.name FROM moneyDb JOIN cateDb ON moneyDb.cateId = cateDb.cateId WHERE (inEx = :inEx) AND (strftime('%Y', moneyDb.date ) = :date)")
    fun getMoneyYear(date: List<String>, inEx: Int): Flow<List<MoneyName>>
}