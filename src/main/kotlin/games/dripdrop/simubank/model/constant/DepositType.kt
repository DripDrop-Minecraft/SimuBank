package games.dripdrop.simubank.model.constant

enum class DepositType(val type: Int) {
    // 活期
    CURRENT(0),

    // 定期
    FIXED(1),

    // 理财产品
    WMP(2)
}