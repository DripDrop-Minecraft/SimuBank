package games.dripdrop.simubank.controller.interfaces

import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.model.constant.Command.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.YELLOW
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal

abstract class AbstractCommandManager : ICommand {
    private val mCommandsForCommon = mutableListOf(
        OVERVIEW.value, DEPOSIT.value, WITHDRAW.value, QUERY_ANNOUNCEMENT.value
    )

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (sender !is Player || args.isNullOrEmpty()) {
            i("cannot execute any command")
            if (args.isNullOrEmpty()) {
                sender.sendMessage("${RED}可输入[/ddbank overview]查看个人账户情况")
            }
            return
        }
        when (args.first()) {
            DEPOSIT.value -> sender.deposit(args)
            WITHDRAW.value -> sender.withdraw(args)
            PUBLISH_ANNOUNCE.value -> sender.publishAnnouncement(args)
            QUERY_ANNOUNCEMENT.value -> sender.queryAnnouncement()
            else -> sender.overview()
        }
    }

    override fun getTabCommandList(sender: CommandSender, args: Array<out String>?): MutableList<String> {
        return if (sender.isOp) mutableListOf<String>().apply {
            addAll(mCommandsForCommon)
            add(PUBLISH_ANNOUNCE.value)
            add(RELOAD.value)
            add(PRODUCT.value)
        } else mCommandsForCommon
    }

    protected fun isLegalAmount(sender: CommandSender, amount: String): Boolean {
        val hint = "${RED}请输入有效金额！"
        return try {
            (amount.all { "0123456789.".contains(it) }
                    && BigDecimal(amount) >= BigDecimal(0.01)
                    && BigDecimal(amount) <= BigDecimal(Double.MAX_VALUE)
                    ).apply {
                    i("is amount [$amount] a legal number: $this")
                    if (!this) {
                        sender.sendMessage(hint)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage(hint)
            false
        }
    }

    protected fun createTopOrBottomBar(): Component {
        return Component.text("$YELLOW========DripDrop在线银行========")
    }

    protected fun createNewLine(amount: Int = 1): Component {
        return Component.text(
            StringBuilder()
                .apply {
                    repeat(amount) {
                        append("\n")
                    }
                }
                .toString()
        )
    }

    protected fun createPartLine(title: String): Component {
        return createNewLine()
            .append(Component.text("$RED--------$title--------"))
            .append(createNewLine())
    }

    protected fun createClickButton(title: String, action: () -> Unit): Component {
        return Component.text(title).clickEvent(ClickEvent.callback { action() })
    }
}