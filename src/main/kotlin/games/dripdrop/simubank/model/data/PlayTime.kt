package games.dripdrop.simubank.model.data

import com.sun.org.apache.xpath.internal.operations.String

data class PlayTime(
    // 玩家UUID
    var playerUUId: String,
    // 上线时间
    var loginTime: Long,
    // 下线时间
    var logoutTime: Long
)