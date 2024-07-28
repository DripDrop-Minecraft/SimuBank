package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.model.data.Announcement
import games.dripdrop.simubank.model.data.Deposit
import games.dripdrop.simubank.model.data.PlayTime
import org.bukkit.configuration.file.YamlConfiguration

object MySQLManager : AbstractDatabaseManager() {
    private var mConfig: YamlConfiguration? = null

    override fun createDatabase(databaseName: String) {
        super.createDatabase(databaseName)
        arrayOf(
            createAnnouncementTable(),
            createPlayTimeTable(),
            createDepositTable()
        ).onEach { statement ->
            getDataSource()?.connection?.use { it.update(statement, mapOf()) {} }
        }
    }

    fun initMySQLDatabase(config: YamlConfiguration) {
        i("start to init database")
        if (isDatabaseInitialized()) {
            i("database has been initialized")
            return
        }
        mConfig = config
        initDatabase(crateHikariConfig(config))
        config.getString("databaseName")?.let { createDatabase(it) }
    }

    private fun crateHikariConfig(config: YamlConfiguration): HikariConfig {
        return HikariConfig().apply {
            jdbcUrl = config.get("databaseUrl").toString()
            username = config.get("databaseAccount").toString()
            password = config.get("databasePassword").toString()
            connectionTimeout = 10 * 1000L
            maximumPoolSize = 16
        }
    }

    private fun createAnnouncementTable(): String = createTableCreatingSQL<Announcement>(
        "timestamp INT PRIMARY KEY, ",
        "title TEXT NOT NULL, ",
        "content TEXT NOT NULL"
    )

    private fun createPlayTimeTable(): String = createTableCreatingSQL<PlayTime>(
        "playerUUId VARCHAR(50) PRIMARY KEY, ",
        "loginTime INT NOT NULL, ",
        "logoutTime INT NOT NULL"
    )

    private fun createDepositTable(): String = createTableCreatingSQL<Deposit>(
        "sn VARCHAR(50) PRIMARY KEY, ",
        "ownerId TEXT NOT NULL, ",
        "amount DOUBLE DEFAULT 0.0, ",
        "createTime INT NOT NULL, ",
        "interestBearingPolicy INT DEFAULT 0, ",
        "interest DOUBLE DEFAULT 0.0, ",
        "type INT DEFAULT 0, ",
        "description TEXT NOT NULL, ",
        "allowEarlyWithdraw BOOL NOT NULL"
    )
}