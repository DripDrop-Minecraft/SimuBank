package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.model.data.Announcement
import games.dripdrop.simubank.model.data.Deposit
import games.dripdrop.simubank.model.data.PlayTime
import org.bukkit.configuration.file.YamlConfiguration
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.PreparedStatement

object MySQLManager : AbstractDatabaseManager() {
    private var mConfig: YamlConfiguration? = null

    override fun createDatabase(databaseName: String) {
        super.createDatabase(databaseName)
        arrayOf(
            createAnnouncementTable(),
            createPlayTimeTable(),
            createDepositTable()
        ).onEach { statement ->
            update(statement)
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

    fun insertPlayTimeData(playTime: PlayTime, callback: (Boolean) -> Unit) {
        i("insert play time data")
        update(
            createTableInsertingSQL<PlayTime>(
                "'${playTime.playerUUId}', ",
                "${playTime.loginTime}, ",
                "${playTime.logoutTime}"
            )
        ) {
            callback(it > 0)
        }
    }

    fun queryPlayTimeByUUID(playerUUID: String = "", callback: (Collection<PlayTime?>) -> Unit) {
        i("query play time data by player's uuid")
        query<PlayTime>(
            StringBuilder("SELECT * FROM ")
                .append(PlayTime::class.java.simpleName)
                .apply {
                    if (playerUUID.isNotEmpty()) {
                        append(" WHERE playerUUID = ?")
                    }
                }.toString(),
            {
                if (playerUUID.isNotEmpty()) {
                    it.setString(1, playerUUID)
                }
            },
            callback
        )
    }

    fun updatePlayTimeByUUID(
        playerUUID: String,
        vararg update: String,
        callback: (Boolean) -> Unit
    ) {
        i("update play time data by player's uuid")
        update(
            StringBuilder("UPDATE ")
                .append(PlayTime::class.java.simpleName.lowercase())
                .append(" SET ")
                .append(*update)
                .append(" WHERE playerUUId = ?")
                .toString(),
            {
                it.setString(1, playerUUID)
            }
        ) {
            callback(it > 0)
        }
    }

    fun insertAnnouncement(announcement: Announcement, callback: (Boolean) -> Unit) {
        i("insert announcement data")
        update(
            createTableInsertingSQL<Announcement>(
                "'${announcement.timestamp}', ",
                "'${announcement.title}', ",
                "'${announcement.content}'"
            )
        ) {
            callback(it > 0)
        }
    }

    fun queryAnnouncementWithAmount(amount: Int = 10, callback: (Collection<Announcement?>) -> Unit) {
        i("query top $amount announcement data")
        query<Announcement>(
            StringBuilder("SELECT * FROM ")
                .append(Announcement::class.java.simpleName)
                .append(" ORDER BY timestamp DESC")
                .append(" LIMIT ?")
                .toString(),
            {
                it.setInt(1, amount)
            },
            callback
        )
    }

    fun insertDepositData(deposit: Deposit, callback: (Boolean) -> Unit) {
        i("insert deposit data")
        update(
            createTableInsertingSQL<Deposit>(
                "'${deposit.sn}', ",
                "'${deposit.ownerId}', ",
                "${BigDecimal(deposit.amount).setScale(2, RoundingMode.FLOOR)}, ",
                "${deposit.interestBearingPolicy}, ",
                "${BigDecimal(deposit.interest).setScale(6, RoundingMode.FLOOR)}, ",
                "${deposit.type}, ",
                "'${deposit.description}', ",
                "${deposit.allowEarlyWithdraw}, ",
                "${deposit.renewal}, ",
                "${deposit.createTime}"
            )
        ) {
            callback(it > 0)
        }
    }

    fun queryDepositByUUID(
        playerUUID: String = "",
        amount: Int = 10,
        callback: (Collection<Deposit?>) -> Unit
    ) {
        i("query deposit data by player uuid")
        query<Deposit>(
            StringBuilder("SELECT * FROM ")
                .append(Deposit::class.java.simpleName)
                .apply {
                    if (playerUUID.isNotEmpty()) {
                        append(" WHERE ownerId = ?")
                    }
                }
                .append(" LIMIT ?")
                .toString(),
            {
                if (playerUUID.isNotEmpty()) {
                    it.setString(1, playerUUID)
                    it.setInt(2, amount)
                } else {
                    it.setInt(1, amount)
                }
            }
        ) {
            callback(it)
        }
    }

    fun updateDepositByPlayerUUID(
        playerUUID: String,
        vararg update: String,
        callback: (Boolean) -> Unit
    ) {
        update(
            StringBuilder("UPDATE ")
                .append(Deposit::class.java.simpleName.lowercase())
                .append(" SET ")
                .append(*update)
                .append(" WHERE ownerId = ?")
                .toString(),
            {
                it.setString(1, playerUUID)
            }
        ) {
            callback(it > 0)
        }
    }

    fun deleteDepositByPlayerUUID(
        setCondition: (PreparedStatement) -> Unit,
        vararg condition: String,
        callback: (Boolean) -> Unit
    ) {
        update(
            StringBuilder("DELETE FROM ")
                .append(Deposit::class.java.simpleName)
                .append(" WHERE ")
                .append(*condition)
                .toString(),
            setCondition
        ) {
            callback(it > 0)
        }
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
        "timestamp VARCHAR(20) PRIMARY KEY, ",
        "title VARCHAR(50) NOT NULL, ",
        "content VARCHAR(140) NOT NULL"
    )

    private fun createPlayTimeTable(): String = createTableCreatingSQL<PlayTime>(
        "playerUUId VARCHAR(40) PRIMARY KEY, ",
        "loginTime LONG NOT NULL, ",
        "logoutTime LONG NOT NULL"
    )

    private fun createDepositTable(): String = createTableCreatingSQL<Deposit>(
        "sn VARCHAR(50) PRIMARY KEY, ",
        "ownerId VARCHAR(36) NOT NULL, ",
        "amount DOUBLE DEFAULT 0.0, ",
        "interestBearingPolicy INT DEFAULT 0, ",
        "interest DOUBLE DEFAULT 0.0, ",
        "type INT DEFAULT 0, ",
        "description VARCHAR(140) NOT NULL, ",
        "allowEarlyWithdraw BOOLEAN DEFAULT true, ",
        "renewal INT DEFAULT 0, ",
        "createTime LONG NOT NULL"
    )
}