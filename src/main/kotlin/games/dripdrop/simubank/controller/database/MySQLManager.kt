package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import games.dripdrop.simubank.controller.utils.i
import org.bukkit.configuration.file.YamlConfiguration
import java.util.concurrent.atomic.AtomicBoolean

object MySQLManager {
    private lateinit var mDataSource: HikariDataSource
    private val mIsInit = AtomicBoolean(false)

    fun initDatabase(config: YamlConfiguration) {
        i("start to init database")
        if (mIsInit.get()) {
            i("database has been initialized")
            return
        }
        try {
            mDataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = config.get("databaseUrl").toString()
                    username = config.get("databaseAccount").toString()
                    password = config.get("databasePassword").toString()
                    connectionTimeout = 10 * 1000L
                    maximumPoolSize = 32
                }
            )
            mIsInit.set(true)
            createDatabase(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDatabase(config: YamlConfiguration) {
        i("try to create database now")
        mDataSource.connection.use {
            it.prepareStatement(
                StringBuilder("CREATE DATABASE IF NOT EXISTS ")
                    .append(config.get("databaseName").toString())
                    .append(" CHARACTER SET utf8mb4")
                    .append(" COLLATE utf8mb4_general_ci")
                    .toString()
            ).executeUpdate()
        }
        mDataSource.connection.use {
            it.prepareStatement(
                StringBuilder("USE ")
                    .append(config.get("databaseName").toString())
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