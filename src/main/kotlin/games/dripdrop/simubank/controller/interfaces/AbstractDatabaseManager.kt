package games.dripdrop.simubank.controller.interfaces

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import games.dripdrop.simubank.controller.database.MySQLManager.insertDepositData
import games.dripdrop.simubank.controller.utils.createDepositSN
import games.dripdrop.simubank.controller.utils.e
import games.dripdrop.simubank.controller.utils.getResultList
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.model.constant.DepositType
import games.dripdrop.simubank.model.data.Deposit
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

    override fun createDatabase() {
        checkInitState {
            i("try to create database now")
            getDataSource()?.connection?.use {
                it.prepareStatement(
                    StringBuilder("CREATE DATABASE IF NOT EXISTS dripdropbank ")
                        .append(" CHARACTER SET utf8mb4")
                        .append(" COLLATE utf8mb4_general_ci")
                        .toString().apply { i("sql is: $this") }
                ).executeUpdate()
            }
            getDataSource()?.connection?.use {
                it.prepareStatement(
                    StringBuilder("USE dripdropbank")
                        .toString().apply { i("sql is: $this") }
                ).executeUpdate()
            }
        }
    }

    // 初始化活期账户
    open fun initCurrentAccount(playerUUID: String, interest: Double) {
        getDataSource()?.connection?.use {
            it.prepareStatement(
                StringBuilder("SELECT * FROM dripdropbank.")
                    .append(Deposit::class.java.simpleName.lowercase())
                    .append("  WHERE ownerId = ?")
                    .append(" AND type = ?")
                    .toString()
            ).apply {
                setString(1, playerUUID)
                setInt(2, DepositType.CURRENT.type)
            }.executeQuery().getResultList<Deposit> { list ->
                if (list.isEmpty()) {
                    i("there should be a new account")
                    insertDepositData(createNewCurrentAccount(playerUUID, interest)) { b ->
                        i("is new current account created: $b")
                    }
                }
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

    protected inline fun <reified T> createPagingQuerySQL(vararg condition: String): String {
        return StringBuilder("SELECT * FROM dripdropbank.")
            .append(T::class.java.simpleName.lowercase())
            .append(*condition)
            .append(" LIMIT ?, ?")
            .toString()
    }

    protected inline fun <reified T> createTableInsertingSQL(vararg values: String): String {
        return StringBuilder("INSERT INTO dripdropbank.")
            .append(T::class.java.simpleName.lowercase())
            .append(" VALUES (")
            .apply { values.onEach { append(it) } }
            .append(")")
            .toString()
    }

    protected inline fun <reified T> createTableCreatingSQL(vararg columns: String): String {
        return StringBuilder("CREATE TABLE IF NOT EXISTS dripdropbank.")
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

    private fun createNewCurrentAccount(playerUUID: String, interest: Double): Deposit {
        return Deposit(
            createDepositSN(playerUUID),
            playerUUID,
            0.00,
            interest,
            type = DepositType.CURRENT.type,
        )
    }
}