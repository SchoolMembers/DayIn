package com.schedule.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schedule.dayin.data.mainD.DiaryDb
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(diary: DiaryDb)

    @Update
    suspend fun update(diary: DiaryDb)

    @Delete
    suspend fun delete(diary: DiaryDb)

    @Query("SELECT * FROM diaryDb WHERE id = :id")
    fun getDiaryById(id: Long): Flow<DiaryDb>

    @Query("SELECT * FROM diarydb ORDER BY date ASC")
    fun getAllDiary(): Flow<List<DiaryDb>>
}