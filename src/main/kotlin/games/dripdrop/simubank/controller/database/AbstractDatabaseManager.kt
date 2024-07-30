package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import games.dripdrop.simubank.controller.interfaces.IDatabase
import games.dripdrop.simubank.controller.utils.e
import games.dripdrop.simubank.controller.utils.getResultList
import games.dripdrop.simubank.controller.utils.i
import java.sql.PreparedStatement
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

    protected fun isDatabaseInitialized(): Boolean = mIsInit.get()

    protected fun issueTransaction(
        isolationLevel: IsolationLevel = IsolationLevel.TRANSACTION_REPEATABLE_READ,
        transaction: () -> Unit
    ) {
        checkInitState {
            getDataSource()?.connection?.use {
                try {
                    it.transactionIsolation = isolationLevel.levelId
                    it.autoCommit = false
                    transaction()
                    it.commit()
                } catch (e: Exception) {
                    e("failed to issue transaction: ${e.localizedMessage}")
                    e.printStackTrace()
                    if (e is SQLException) {
                        it.rollback()
                    }
                } finally {
                    it.autoCommit = true
                }
            }
        }
    }

    protected fun batch(
        sql: String,
        execute: (PreparedStatement) -> Unit = {},
        callback: (IntArray) -> Unit = {}
    ) {
        i("sql = $sql")
        checkInitState {
            getDataSource()?.connection?.use {
                it.prepareStatement(sql).apply {
                    execute(this)
                }.executeBatch().apply { callback(this) }
            }
        }
    }

    protected inline fun <reified T> query(
        sql: String,
        crossinline setCondition: (PreparedStatement) -> Unit = {},
        crossinline callback: (Collection<T?>) -> Unit = {}
    ) {
        i("sql = $sql")
        checkInitState {
            getDataSource()?.connection?.use {
                it.prepareStatement(sql).apply {
                    setCondition(this)
                }.executeQuery().getResultList<T>(callback)
            }
        }
    }

    protected fun update(
        sql: String,
        setCondition: (PreparedStatement) -> Unit = {},
        callback: (Int) -> Unit = {}
    ) {
        i("sql = $sql")
        checkInitState {
            getDataSource()?.connection?.use {
                it.prepareStatement(sql).apply {
                    setCondition(this)
                }.executeUpdate().apply { callback(this) }
            }
        }
    }

    protected inline fun <reified T> createTableInsertingSQL(vararg values: String): String {
        return StringBuilder("INSERT INTO ")
            .append(T::class.java.simpleName.lowercase())
            .append(" VALUES (")
            .apply { values.onEach { append(it) } }
            .append(")")
            .toString()
    }

    protected inline fun <reified T> createTableCreatingSQL(vararg columns: String): String {
        return StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(T::class.java.simpleName.lowercase())
            .append(" (")
            .apply { columns.forEach { append(it) } }
            .append(")")
            .toString()
    }

    protected fun checkInitState(action: () -> Unit) {
        if (!isDatabaseInitialized()) {
            throw IllegalStateException("database has not been initialized yet")
        }
        action()
    }
}