package games.dripdrop.simubank.controller.database

import com.zaxxer.hikari.HikariConfig
import games.dripdrop.simubank.controller.interfaces.AbstractDatabaseManager
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.model.constant.DepositType
import games.dripdrop.simubank.model.constant.ErrorCode
import games.dripdrop.simubank.model.data.Announcement
import games.dripdrop.simubank.model.data.Deposit
import games.dripdrop.simubank.model.data.PlayTime
import org.bukkit.configuration.file.YamlConfiguration
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.PreparedStatement

object MySQLManager : AbstractDatabaseManager() {
    private var mConfig: YamlConfiguration? = null

    override fun createDatabase() {
        super.createDatabase()
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
        createDatabase()
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
            StringBuilder("SELECT * FROM dripdropbank.")
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
            StringBuilder("UPDATE dripdropbank.")
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

    fun queryAnnouncementWithPaging(
        offset: Int = 0,
        limit: Int = 5,
        callback: (Collection<Announcement?>) -> Unit
    ) {
        i("query announcement data with paging")
        query<Announcement>(
            createPagingQuerySQL<Announcement>(" ORDER BY timestamp DESC"),
            {
                it.setInt(1, offset)
                it.setInt(2, limit)
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
                "${deposit.updateTime}, ",
                "${deposit.createTime}"
            )
        ) {
            callback(it > 0)
        }
    }

    fun queryDepositByUUIDWithPaging(
        playerUUID: String = "",
        type: Int = DepositType.FIXED.type,
        offset: Int = 0,
        amount: Int = 5,
        callback: (Collection<Deposit?>) -> Unit
    ) {
        i("query deposit data by player uuid with paging")
        query<Deposit>(
            createPagingQuerySQL<Deposit>(
                (if (playerUUID.isNotEmpty()) " WHERE ownerId = ?" else ""),
                (if (playerUUID.isNotEmpty()) " AND type = ?" else " WHERE type = ?"),
                (if (DepositType.FIXED.type == type) " AND amount > 0.00" else ""),
                " ORDER BY updateTime DESC"
            ),
            {
                if (playerUUID.isNotEmpty()) {
                    it.setString(1, playerUUID)
                    it.setInt(2, type)
                    it.setInt(3, offset)
                    it.setInt(4, amount)
                } else {
                    it.setInt(1, type)
                    it.setInt(2, offset)
                    it.setInt(3, amount)
                }
            },
            callback
        )
    }

    fun queryFixedDepositByPlayerUUIDAndSN(
        playerUUID: String,
        sn: String,
        callback: (Collection<Deposit?>) -> Unit
    ) {
        query<Deposit>(
            StringBuilder("SELECT * FROM dripdropbank.")
                .append(Deposit::class.java.simpleName.lowercase())
                .append(" WHERE ownerId = ?")
                .append(" AND sn = ?")
                .toString(),
            {
                it.setString(1, playerUUID)
                it.setString(2, sn)
            },
            callback
        )
    }

    fun updateCurrentDepositAmountWithUUID(
        playerUUID: String,
        diff: Double,
        callback: (ErrorCode) -> Unit
    ) {
        queryDepositByUUIDWithPaging(playerUUID, DepositType.CURRENT.type) { list ->
            if (list.isEmpty()) {
                callback(ErrorCode.NO_CURRENT_ACCOUNT)
            }
            list.firstOrNull()?.apply {
                if (amount + diff >= 0.0) {
                    amount += diff
                    updateDepositBySN(sn, "amount = $amount") { callback(ErrorCode.SUCCESS) }
                } else {
                    callback(ErrorCode.INSUFFICIENT_FUND)
                }
            }
        }
    }

    fun updateFixedDepositAmountBySN(
        playerUUID: String,
        sn: String,
        diff: Double,
        callback: (ErrorCode) -> Unit
    ) {
        queryFixedDepositByPlayerUUIDAndSN(playerUUID, sn) { list ->
            if (list.isEmpty()) {
                callback(ErrorCode.NO_SUCH_DEPOSIT)
            }
            list.firstOrNull()?.apply {
                when {
                    amount + diff < 0.00 -> {
                        callback(ErrorCode.INSUFFICIENT_FUND)
                    }

                    amount + diff == 0.00 -> {
                        amount += diff
                        updateDepositBySN(sn, "amount = $amount") {
                            if (it) {
                                callback(ErrorCode.ALL_WITHDRAW)
                            } else {
                                callback(ErrorCode.UNKNOWN)
                            }
                        }
                    }

                    amount + diff > 0.00 -> {
                        amount += diff
                        updateDepositBySN(sn, "amount = $amount") { callback(ErrorCode.SUCCESS) }
                    }
                }
            }
        }
    }

    fun updateDepositBySN(
        sn: String,
        vararg update: String,
        callback: (Boolean) -> Unit
    ) {
        update(
            StringBuilder("UPDATE dripdropbank.")
                .append(Deposit::class.java.simpleName.lowercase())
                .append(" SET ")
                .append(*update)
                .append(", updateTime = ${System.currentTimeMillis()}")
                .append(" WHERE sn = ?")
                .toString(),
            {
                it.setString(1, sn)
            }
        ) {
            callback(it > 0)
        }
    }

    fun updateDepositByPlayerUUID(
        playerUUID: String,
        type: Int = DepositType.FIXED.type,
        vararg update: String,
        callback: (Boolean) -> Unit
    ) {
        update(
            StringBuilder("UPDATE dripdropbank.")
                .append(Deposit::class.java.simpleName.lowercase())
                .append(" SET ")
                .append(*update)
                .append(", updateTime = ${System.currentTimeMillis()}")
                .append(" WHERE ownerId = ?")
                .append(" AND type = ?")
                .toString(),
            {
                it.setString(1, playerUUID)
                it.setInt(2, type)
            }
        ) {
            callback(it > 0)
        }
    }

    fun deleteDepositWithConditions(
        setCondition: (PreparedStatement) -> Unit,
        vararg condition: String,
        callback: (Boolean) -> Unit
    ) {
        update(
            StringBuilder("DELETE FROM dripdropbank.")
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
        "updateTime LONG NOT NULL, ",
        "createTime LONG NOT NULL"
    )
}