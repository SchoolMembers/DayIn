package com.example.dayin.data.mainD.repository

import com.example.dayin.data.mainD.CateDb
import com.example.dayin.data.mainD.dao.CateDbDao
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

    fun allCateItems(): Flow<List<CateDb>> {
        return cateDbDao.getAllCate()
    }

    fun getCateById(cateId: Long): Flow<CateDb> {
        return cateDbDao.getCateById(cateId)
    }

    fun getCateByInEx(inEx: Int): Flow<List<CateDb>> {
        return cateDbDao.getCateByInEx(inEx)
    }
}