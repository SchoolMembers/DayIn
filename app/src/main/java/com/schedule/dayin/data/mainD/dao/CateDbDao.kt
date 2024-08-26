package com.schedule.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schedule.dayin.data.mainD.CateDb
import kotlinx.coroutines.flow.Flow

@Dao
interface CateDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cate: CateDb)

    @Update
    suspend fun update(cate: CateDb)

    @Delete
    suspend fun delete(cate: CateDb)

    @Query("DELETE FROM cateDb WHERE cateId = :cateId")
    suspend fun deleteCateById(cateId: Long)

    @Query("SELECT * FROM cateDb WHERE cateId = :cateId")
    fun getCateById(cateId: Long): CateDb

    @Query("SELECT * FROM cateDb ORDER BY inEx, cateId ASC")
    fun getAllCate(): Flow<List<CateDb>>

    @Query("SELECT * FROM cateDb WHERE inEx = :inEx ORDER BY cateId DESC")
    fun getCateByInEx(inEx: Int): Flow<List<CateDb>>

    @Query("SELECT * FROM cateDb WHERE cateId >= 25 ORDER BY cateId ASC")
    fun getUserCate(): Flow<List<CateDb>>
}