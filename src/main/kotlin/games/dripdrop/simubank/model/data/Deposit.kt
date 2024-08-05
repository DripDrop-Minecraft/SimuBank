package games.dripdrop.simubank.model.data

import games.dripdrop.simubank.model.constant.DepositPolicy
import games.dripdrop.simubank.model.constant.DepositType

data class Deposit(
    // 存单编号
    val sn: String,
    // 存单所有者标识符（玩家UUID）
    val ownerId: String,
    // 存单金额
    var amount: Double,
    // 存单利率
    val interest: Double,
    // 计息策略
    val interestBearingPolicy: Int = DepositPolicy.ACCUMULATE_BY_TIMESTAMP.policy,
    // 存单对应的产品类型
    val type: Int = DepositType.FIXED.type,
    // 存单描述
    var description: String = "",
    // 是否允许提前支取
    val allowEarlyWithdraw: Boolean = true,
    // 续期次数
    var renewal: Int = 0,
    // 存单更新时间
    var updateTime: Long = System.currentTimeMillis(),
    // 存单创建时间
    val createTime: Long = System.currentTimeMillis()
)
