package games.dripdrop.simubank.model.data

data class Deposit(
    // 存单编号
    val sn: String,
    // 存单所有者标识符（玩家UUID）
    val ownerId: String,
    // 存单金额
    val amount: Double,
    // 存单创建时间
    val createTime: Long,
    // 累计存款时间
    val depositKeepingTime: Double,
    // 存单利率
    val interest: Double,
    // 存单种类
    val type: Int,
    // 存单描述
    val desc: String,
    // 是否允许提前支取
    val allowEarlyWithdraw: Boolean
)
