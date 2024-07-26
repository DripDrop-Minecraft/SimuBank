package games.dripdrop.simubank.model.data

data class PlayTime(
    // 玩家UUID
    var playerUUId: String,
    // 上线时间
    var loginTime: Long,
    // 下线时间
    var logoutTime: Long
)