package games.dripdrop.simubank.controller.interfaces

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

interface IDatabase {
    // 初始化数据库连接
    fun initDatabase(config: HikariConfig)

    // 销毁数据库连接
    fun deInitDatabase()

    // 获取连接池
    fun getDataSource(): HikariDataSource?

    fun createDatabase()
}