package com.example.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dayin.data.mainD.ScheduleDb
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(scheduleDb: ScheduleDb)

    @Update
    suspend fun update(scheduleDb: ScheduleDb)

    @Delete
    suspend fun delete(scheduleDb: ScheduleDb)

    @Query("SELECT * from scheduleDb WHERE id = :id")
    fun getSche(id: Long): Flow<ScheduleDb>

    @Query("SELECT * from scheduleDb ORDER BY date ASC")
    fun getAllSche(): Flow<List<ScheduleDb>>

    @Query("SELECT * from scheduleDb WHERE auto <> 0 ORDER BY title ASC")
    fun getAutoSche(): Flow<List<ScheduleDb>>
}