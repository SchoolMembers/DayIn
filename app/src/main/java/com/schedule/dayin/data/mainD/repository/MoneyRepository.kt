package com.schedule.dayin.data.mainD.repository

import com.schedule.dayin.data.mainD.MoneyAndCate
import com.schedule.dayin.data.mainD.MoneyDb
import com.schedule.dayin.data.mainD.dao.MoneyDbDao
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

    suspend fun deleteMoneyByCateId(cateId: Long) {
        moneyDbDao.deleteMoneyByCateId(cateId)
    }

    fun allMoney(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getAllMoney()
    }

    fun getMoney(id: Long): Flow<MoneyAndCate> {
        return moneyDbDao.getItem(id)
    }

    fun onlyMoney(id: Long): MoneyDb {
        return moneyDbDao.getOnlyMoney(id)
    }

    fun getUserCate(): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getUserCate()
    }

    fun getDayMoney(startDate: Long, endDate: Long): List<MoneyAndCate> {
        return moneyDbDao.getMoneyDay(startDate, endDate)
    }

    fun getAutoMoney(): List<MoneyAndCate> {
        return moneyDbDao.getAutoMoney()
    }

    fun getMoneyMonthData(startDate: Long, endDate: Long): List<MoneyAndCate> {
        return moneyDbDao.getMoneyMonth(startDate, endDate)
    }

    fun onlyMoneyMonthData(startDate: Long, endDate: Long, inEx: Int): List<MoneyDb> {
        return moneyDbDao.onlyMoneyMonth(startDate, endDate, inEx)
    }

    fun getMoneyYearData(date: List<String>, inEx: Int): Flow<List<MoneyAndCate>> {
        return moneyDbDao.getMoneyYear(date, inEx)
    }

    fun getAnalCate(startDate: Long, endDate: Long, index: Long): List<MoneyAndCate> {
        return moneyDbDao.getAnalCate(startDate, endDate, index)
    }
}
