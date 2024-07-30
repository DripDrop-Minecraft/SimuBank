package games.dripdrop.simubank.model.data

import java.text.SimpleDateFormat
import java.util.*

data class Announcement(
    // 公告发布时间戳
    val timestamp: String = "A${SimpleDateFormat("yyMMddHHmmssSSS").format(Date())}",
    // 公告标题
    var title: String,
    // 公告内容
    var content: String
)