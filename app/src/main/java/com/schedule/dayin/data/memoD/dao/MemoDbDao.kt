package com.schedule.dayin.data.memoD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schedule.dayin.data.memoD.MemoDb
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(memoDb: MemoDb)

    @Update
    suspend fun update(memoDb: MemoDb)

    @Delete
    suspend fun delete(memoDb: MemoDb)

    @Query("SELECT * from memoDb ORDER BY id ASC")
    fun getAllMemo(): Flow<List<MemoDb>>

    @Query("SELECT * from memoDb WHERE id = :id")
    fun getMemo(id: Long): MemoDb

    @Query("SELECT * from memoDb ORDER BY id ASC")
    fun getMemoTitle(): Flow<List<MemoDb>>
}