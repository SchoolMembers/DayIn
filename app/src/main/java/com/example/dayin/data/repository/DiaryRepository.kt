package com.example.dayin.data.repository

import com.example.dayin.data.DiaryDb
import com.example.dayin.data.dao.DiaryDbDao
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
}