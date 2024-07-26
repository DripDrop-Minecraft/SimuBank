package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import games.dripdrop.simubank.controller.utils.getPluginConfig
import games.dripdrop.simubank.controller.utils.i
import java.util.concurrent.atomic.AtomicBoolean

object MySQLManager {
    private lateinit var mDataSource: HikariDataSource
    private val mIsInit = AtomicBoolean(false)

    fun initDatabase() {
        i("start to init database")
        if (mIsInit.get()) {
            i("database has been initialized")
            return
        }
        try {
            mDataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = getPluginConfig()?.get("databaseUrl").toString()
                    username = getPluginConfig()?.get("databaseAccount").toString()
                    password = getPluginConfig()?.get("databasePassword").toString()
                    connectionTimeout = 10 * 1000L
                    maximumPoolSize = 32
                }
            )
            mIsInit.set(true)
            createDatabase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDatabase() {
        i("try to create database now")
        mDataSource.connection.use {
            it.prepareStatement(
                StringBuilder("CREATE DATABASE IF NOT EXISTS ")
                    .append(getPluginConfig()?.get("databaseName").toString())
                    .append(" CHARACTER SET utf8mb4")
                    .append(" COLLATE utf8mb4_general_ci")
                    .toString()
            ).executeUpdate()
        }
        mDataSource.connection.use {
            it.prepareStatement(
                StringBuilder("USE ")
                    .append(getPluginConfig()?.get("databaseName").toString())
                    .toString()
            ).executeUpdate()
        }
        createTables()
    }

    private fun createTables() {
        i("try to create tables now")
        // TODO
    }
}