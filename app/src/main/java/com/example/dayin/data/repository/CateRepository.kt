package com.example.dayin.data.repository

import com.example.dayin.data.CateDb
import com.example.dayin.data.dao.CateDbDao
import kotlinx.coroutines.flow.Flow

class CateRepository(private val cateDbDao: CateDbDao) {

    suspend fun insertItem(cate: CateDb) {
        cateDbDao.insert(cate)
    }

    suspend fun updateItem(cate: CateDb) {
        cateDbDao.update(cate)
    }

    suspend fun deleteItem(cate: CateDb) {
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