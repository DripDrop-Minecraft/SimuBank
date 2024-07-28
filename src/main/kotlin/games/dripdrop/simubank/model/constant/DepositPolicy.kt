package games.dripdrop.simubank.model.constant

enum class DepositPolicy(val policy: Int) {
    // 简单计息法：利息 = 向下圆整((当前时间 - 存单创建时间)) / 1d) × 本金 × 利率
    ACCUMULATE_BY_TIMESTAMP(0),

    // 累进计息法：利息 = 向下圆整(创建存单至今的累积在线时长 / 1d) × 本金 × 利率
    ACCUMULATE_BY_ONLINE_TIME(1)
}