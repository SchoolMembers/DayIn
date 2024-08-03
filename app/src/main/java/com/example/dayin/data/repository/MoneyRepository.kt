package com.example.dayin.data.repository

import com.example.dayin.data.MoneyAndCate
import com.example.dayin.data.MoneyDb
import com.example.dayin.data.dao.MoneyDbDao
import kotlinx.coroutines.flow.Flow

class MoneyRepository(private val moneyDbDao: MoneyDbDao) {

    suspend fun insertItem(money: MoneyDb) {
        moneyDbDao.insert(money)
    }

    suspend fun updateItem(money: MoneyDb) {
        moneyDbDao.update(money)
    }

    suspend fun deleteItem(money: MoneyDb) {
        moneyDbDao.delete(money)
    }

    fun allMoneyItems(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getAllItems()
    }

    fun getMoneyItemById(id: Long): Flow<MoneyAndCate> {
        return moneyDbDao.getItem(id)
    }

    fun getDayMoney(date: String): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getDay(date)
    }

    fun getAutoMoneyItems(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getAutoItems()
    }

    fun getMonthData(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getMonth(date, inEx)
    }

    fun getYearData(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getYear(date, inEx)
    }
}
