package com.example.dayin.data.repository

import com.example.dayin.data.ScheduleDb
import com.example.dayin.data.dao.ScheduleDbDao
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDbDao: ScheduleDbDao) {

    suspend fun insertItem(schedule: ScheduleDb) {
        scheduleDbDao.insert(schedule)
    }

    suspend fun updateItem(schedule: ScheduleDb) {
        scheduleDbDao.update(schedule)
    }

    suspend fun deleteItem(schedule: ScheduleDb) {
        scheduleDbDao.delete(schedule)
    }

    fun allSchedules(): Flow<List<ScheduleDb>> {
        return scheduleDbDao.getAllItems()
    }

    fun getScheduleById(id: Long): Flow<ScheduleDb> {
        return scheduleDbDao.getItem(id)
    }

    fun getAutoSchedules(): Flow<List<ScheduleDb>> {
        return scheduleDbDao.getAutoItems()
    }
}
