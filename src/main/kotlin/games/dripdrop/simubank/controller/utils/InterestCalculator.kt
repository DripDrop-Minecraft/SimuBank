package games.dripdrop.simubank.controller.utils

import games.dripdrop.simubank.model.constant.DepositPolicy
import games.dripdrop.simubank.model.data.Deposit
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.pow

object InterestCalculator {
    private const val ONE_DAY = 1.0 * 24 * 60 * 60 * 1000L

    fun calculate(deposit: Deposit): Deposit {
        return if (DepositPolicy.ACCUMULATE_BY_TIMESTAMP.policy == deposit.interestBearingPolicy) {
            simpleCalc(deposit)
        } else {
            accumulateCalc(deposit)
        }
    }

    private fun simpleCalc(deposit: Deposit): Deposit {
        return deposit.apply {
            updateTime = System.currentTimeMillis()
            amount = BigDecimal(
                (1 + interest.toPercent()).pow(abs(updateTime - createTime) / ONE_DAY) * amount
            ).setScale(2, RoundingMode.FLOOR).toDouble()
        }
    }

    private fun accumulateCalc(deposit: Deposit): Deposit {
        return deposit.apply {
            updateTime = System.currentTimeMillis()
            // TODO
        }
    }

    private fun Double.toPercent(): Double = this / 100.0
}