package com.example.dayin.data.memoD.repository

import com.example.dayin.data.memoD.MemoDb
import com.example.dayin.data.memoD.TitleData
import com.example.dayin.data.memoD.dao.MemoDbDao
import kotlinx.coroutines.flow.Flow

class MemoRepository(private val memoDao: MemoDbDao) {

    suspend fun insertMemo(memo: MemoDb) {
        memoDao.insert(memo)
    }

    suspend fun updateMemo(memo: MemoDb) {
        memoDao.update(memo)
    }

    suspend fun deleteMemo(memo: MemoDb) {
        memoDao.delete(memo)
    }

    fun getAllMemos(): Flow<List<MemoDb>> {
        return memoDao.getAllMemo()
    }

    fun getMemoById(memoId: Long): Flow<MemoDb> {
        return memoDao.getMemo(memoId)
    }

    fun getMemoTitles(): Flow<List<TitleData>> {
        return memoDao.getMemoTitle()
    }
}