package com.example.dayin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dayin.data.CateDb
import kotlinx.coroutines.flow.Flow

@Dao
interface CateDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cate: CateDb)

    @Update
    suspend fun update(cate: CateDb)

    @Delete
    suspend fun delete(cate: CateDb)

    @Query("SELECT * FROM cateDb WHERE cateId = :cateId")
    fun getCateById(cateId: Long): Flow<CateDb>

    @Query("SELECT * FROM cateDb ORDER BY inEx, cateId ASC")
    fun getAllCate(): Flow<List<CateDb>>

    @Query("SELECT * FROM cateDb WHERE inEx = :inEx ORDER BY cateId ASC")
    fun getCateByInEx(inEx: Int): Flow<List<CateDb>>
}