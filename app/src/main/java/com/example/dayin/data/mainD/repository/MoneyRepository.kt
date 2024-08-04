package com.example.dayin.data.mainD.repository

import com.example.dayin.data.mainD.MoneyAndCate
import com.example.dayin.data.mainD.MoneyDb
import com.example.dayin.data.mainD.dao.MoneyDbDao
import kotlinx.coroutines.flow.Flow

class MoneyRepository(private val moneyDbDao: MoneyDbDao) {

    suspend fun insertMoney(money: MoneyDb) {
        moneyDbDao.insert(money)
    }

    suspend fun updateMoney(money: MoneyDb) {
        moneyDbDao.update(money)
    }

    suspend fun deleteMoney(money: MoneyDb) {
        moneyDbDao.delete(money)
    }

    fun allMoney(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getAllMoney()
    }

    fun getMoney(id: Long): Flow<MoneyAndCate> {
        return moneyDbDao.getItem(id)
    }

    fun getDayMoney(date: String): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getMoneyDay(date)
    }

    fun getAutoMoney(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getAutoMoney()
    }

    fun getMoneyMonthData(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getMoneyMonth(date, inEx)
    }

    fun getMoneyYearData(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getMoneyYear(date, inEx)
    }
}
