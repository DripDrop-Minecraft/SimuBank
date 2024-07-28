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
    // 计息策略
    val interestBearingPolicy: Int,
    // 存单利率
    val interest: Double,
    // 存单对应的产品类型
    val type: Int,
    // 存单描述
    val description: String,
    // 是否允许提前支取
    val allowEarlyWithdraw: Boolean
)
