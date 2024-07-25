package games.dripdrop.simubank.model.data

data class Product(
    // 产品编号
    val sn: String,
    // 产品名称
    val name: String,
    // 产品描述
    val desc: String,
    // 利率
    val interest: Double,
    // 起购金额
    val minimumAmount: Double,
    // 剩余可购额度，-1.0代表无限制
    val availableQuota: Double,
    // 持有时间（天数）
    val keepingTime: Int,
    // 是否允许提前支取
    val allowEarlyWithdraw: Boolean
)
