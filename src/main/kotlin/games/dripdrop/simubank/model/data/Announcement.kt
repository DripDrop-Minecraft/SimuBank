package games.dripdrop.simubank.model.data

data class Announcement(
    // 公告发布时间戳
    val timestamp: Long = System.currentTimeMillis(),
    // 公告标题
    var title: String,
    // 公告内容
    var content: String
)