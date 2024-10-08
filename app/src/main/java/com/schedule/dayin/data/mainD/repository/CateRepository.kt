package com.schedule.dayin.data.mainD.repository

import com.schedule.dayin.data.mainD.CateDb
import com.schedule.dayin.data.mainD.dao.CateDbDao
import kotlinx.coroutines.flow.Flow

class CateRepository(private val cateDbDao: CateDbDao) {

    suspend fun insertCate(cate: CateDb) {
        cateDbDao.insert(cate)
    }

    suspend fun updateCate(cate: CateDb) {
        cateDbDao.update(cate)
    }

    suspend fun deleteCate(cate: CateDb) {
        cateDbDao.delete(cate)
    }

    suspend fun deleteCateById(cateId: Long) {
        cateDbDao.deleteCateById(cateId)
    }


    fun allCateItems(): Flow<List<CateDb>> {
        return cateDbDao.getAllCate()
    }

    fun getCateById(cateId: Long): CateDb {
        return cateDbDao.getCateById(cateId)
    }

    fun getCateByInEx(inEx: Int): Flow<List<CateDb>> {
        return cateDbDao.getCateByInEx(inEx)
    }

    fun getUserCate(): Flow<List<CateDb>> {
        return cateDbDao.getUserCate()
    }

    fun getUserCateInex(inEx: Int): List<CateDb> {
        return cateDbDao.getUserCateInex(inEx)
    }

    fun getUserCateInex2(inEx: Int): Flow<List<CateDb>> {
        return cateDbDao.getUserCateInex2(inEx)
    }
}