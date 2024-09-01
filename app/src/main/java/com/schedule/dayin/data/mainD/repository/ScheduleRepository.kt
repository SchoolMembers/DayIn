package com.schedule.dayin.data.mainD.repository

import com.schedule.dayin.data.mainD.ScheduleDb
import com.schedule.dayin.data.mainD.dao.ScheduleDbDao
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

    fun getTimes(startDate: Long, endDate: Long): List<ScheduleDb> {
        return scheduleDbDao.getTitleTime(startDate, endDate)
    }

    fun getLast(): List<ScheduleDb> {
        return scheduleDbDao.getLast()
    }
}
