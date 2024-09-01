package com.schedule.dayin.data.mainD.repository

import com.schedule.dayin.data.mainD.DiaryDb
import com.schedule.dayin.data.mainD.dao.DiaryDbDao
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDbDao: DiaryDbDao) {

    suspend fun insertDiary(diary: DiaryDb) {
        diaryDbDao.insert(diary)
    }

    suspend fun updateDiary(diary: DiaryDb) {
        diaryDbDao.update(diary)
    }

    suspend fun deleteDiary(diary: DiaryDb) {
        diaryDbDao.delete(diary)
    }

    fun allDiaryItems(): Flow<List<DiaryDb>> {
        return diaryDbDao.getAllDiary()
    }

    fun getDiaryById(id: Long): Flow<DiaryDb> {
        return diaryDbDao.getDiaryById(id)
    }

    fun getTime(startDate: Long, endDate: Long): DiaryDb {
        return diaryDbDao.getTime(startDate, endDate)
    }
}