package games.dripdrop.simubank.model.data

data class Product(
    // 产品代码
    val productCode: String,
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
    // 最低持有时间（天数），-1.0表示无限制
    val minimumKeepingTime: Int,
    // 是否允许提前支取
    val allowEarlyWithdraw: Boolean
)
