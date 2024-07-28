package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import games.dripdrop.simubank.controller.interfaces.IDatabase
import games.dripdrop.simubank.controller.utils.d
import games.dripdrop.simubank.controller.utils.e
import games.dripdrop.simubank.controller.utils.i
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractDatabaseManager : IDatabase {
    private var mDataSource: HikariDataSource? = null
    private val mIsInit = AtomicBoolean(false)

    override fun initDatabase(config: HikariConfig) {
        if (mDataSource != null && true == mDataSource?.isRunning) {
            i("database has been initialized")
            return
        }
        try {
            mDataSource = HikariDataSource(config)
            mIsInit.set(true)
        } catch (e: Exception) {
            e("failed to init database connection: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    override fun deInitDatabase() {
        i("deinit database")
        mDataSource?.close()
        mDataSource = null
    }

    override fun getDataSource(): HikariDataSource? {
        i("is datasource null: ${mDataSource == null}")
        return mDataSource
    }

    override fun createDatabase(databaseName: String) {
        checkInitState {
            i("try to create database now")
            getDataSource()?.connection?.use {
                it.prepareStatement(
                    StringBuilder("CREATE DATABASE IF NOT EXISTS ")
                        .append(databaseName)
                        .append(" CHARACTER SET utf8mb4")
                        .append(" COLLATE utf8mb4_general_ci")
                        .toString()
                ).executeUpdate()
            }
            getDataSource()?.connection?.use {
                it.prepareStatement(
                    StringBuilder("USE ")
                        .append(databaseName)
                        .toString()
                ).executeUpdate()
            }
        }
    }

    override fun Connection.query(sql: String, map: Map<Int, Any>, callback: (ResultSet) -> Unit) {
        checkInitState {
            d("sql of query: $sql")
            use {
                prepareStatement(sql).apply {
                    map.forEach { (k, v) -> setObject(k, v) }
                    callback(executeQuery())
                }
            }
        }
    }

    override fun Connection.update(sql: String, map: Map<Int, Any>, callback: (Int) -> Unit) {
        checkInitState {
            d("sql of update: $sql")
            use {
                prepareStatement(sql).apply {
                    map.forEach { (k, v) -> setObject(k, v) }
                    callback(executeUpdate())
                }
            }
        }
    }

    override fun Connection.batch(sql: String, callback: (PreparedStatement) -> Unit) {
        checkInitState {
            d("sql of batch: $sql")
            use {
                prepareStatement(sql).use {
                    callback(it)
                    it.executeBatch().apply { i("size of batch results = $size") }
                }
            }
        }
    }

    override fun Connection.issueTransaction(isolationLevel: IsolationLevel, transaction: () -> Unit) {
        checkInitState {
            use {
                try {
                    transactionIsolation = isolationLevel.levelId
                    autoCommit = false
                    transaction()
                    commit()
                } catch (e: Exception) {
                    e("failed to issue transaction: ${e.localizedMessage}")
                    e.printStackTrace()
                    if (e is SQLException) {
                        rollback()
                    }
                } finally {
                    autoCommit = true
                }
            }
        }
    }

    protected fun isDatabaseInitialized(): Boolean = mIsInit.get()

    protected inline fun <reified T> createTableCreatingSQL(vararg columns: String): String {
        return StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(T::class.java.simpleName.lowercase())
            .append(" (")
            .apply { columns.forEach { append(it) } }
            .append(")")
            .toString()
    }

    private fun checkInitState(action: () -> Unit) {
        if (!isDatabaseInitialized()) {
            throw IllegalStateException("database has not been initialized yet")
        }
        action()
    }
}