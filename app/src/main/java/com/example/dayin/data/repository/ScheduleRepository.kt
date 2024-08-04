package com.example.dayin.data.repository

import com.example.dayin.data.ScheduleDb
import com.example.dayin.data.dao.ScheduleDbDao
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDbDao: ScheduleDbDao) {

    suspend fun insertSche(schedule: ScheduleDb) {
        scheduleDbDao.insert(schedule)
    }

    suspend fun updateSche(schedule: ScheduleDb) {
        scheduleDbDao.update(schedule)
    }

    suspend fun deleteSche(schedule: ScheduleDb) {
        scheduleDbDao.delete(schedule)
    }

    fun allSchedules(): Flow<List<ScheduleDb>> {
        return scheduleDbDao.getAllSche()
    }

    fun getScheduleById(id: Long): Flow<ScheduleDb> {
        return scheduleDbDao.getSche(id)
    }

    fun getAutoSchedules(): Flow<List<ScheduleDb>> {
        return scheduleDbDao.getAutoSche()
    }
}
