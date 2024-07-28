package games.dripdrop.simubank.controller.interfaces

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

interface IDatabase {
    // 初始化数据库连接
    fun initDatabase(config: HikariConfig)

    // 销毁数据库连接
    fun deInitDatabase()

    // 获取连接池
    fun getDataSource(): HikariDataSource?

    fun createDatabase(databaseName: String)

    // 执行SQL语句查询
    fun Connection.query(sql: String, map: Map<Int, Any>, callback: (ResultSet) -> Unit)

    // 执行SQL语句更新
    fun Connection.update(sql: String, map: Map<Int, Any>, callback: (Int) -> Unit)

    // 批量执行SQL语句
    fun Connection.batch(sql: String, callback: (PreparedStatement) -> Unit)

    // 执行SQL语句事务
    fun Connection.issueTransaction(
        isolationLevel: IsolationLevel = IsolationLevel.TRANSACTION_REPEATABLE_READ,
        transaction: () -> Unit
    )
}