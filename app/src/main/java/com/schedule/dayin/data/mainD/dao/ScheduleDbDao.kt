package com.schedule.dayin.data.mainD.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schedule.dayin.data.mainD.ScheduleDb
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(scheduleDb: ScheduleDb)

    @Update
    suspend fun update(scheduleDb: ScheduleDb)

    @Delete
    suspend fun delete(scheduleDb: ScheduleDb)

    @Query("DELETE FROM ScheduleDb WHERE auto = :auto AND title = :title")
    suspend fun deleteAuto(title: String, auto: Int)

    @Query("SELECT * from scheduleDb WHERE id = :id")
    fun getSche(id: Long): Flow<ScheduleDb>

    @Query("SELECT * from scheduleDb ORDER BY date ASC")
    fun getAllSche(): Flow<List<ScheduleDb>>

    @Query("SELECT * from scheduleDb WHERE auto <> 0 ORDER BY title ASC")
    fun getAutoSche(): List<ScheduleDb>

    @Query("SELECT * from scheduleDb WHERE :startDate <= date and date <= :endDate ORDER BY time, date, id ASC")
    fun getTitleTime(startDate: Long, endDate: Long): List<ScheduleDb>

    @Query("SELECT * FROM scheduleDb ORDER BY id DESC LIMIT 1")
    fun getLast(): List<ScheduleDb>

    @Query("SELECT * from scheduleDb WHERE auto = :id ORDER BY title ASC")
    fun getAutoIdSche(id: Int): List<ScheduleDb>

    @Query("SELECT * from scheduleDb WHERE auto = :id AND title = :title ORDER BY date ASC")
    fun getAutoTitleSche(title: String, id: Int): List<ScheduleDb>
}