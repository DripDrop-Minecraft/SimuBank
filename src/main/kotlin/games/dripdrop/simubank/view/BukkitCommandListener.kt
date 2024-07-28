package games.dripdrop.simubank.view

import games.dripdrop.simubank.controller.interfaces.ICommand
import games.dripdrop.simubank.controller.utils.COMMAND
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.controller.utils.pluginLang
import games.dripdrop.simubank.model.constant.Command.*
import games.dripdrop.simubank.model.constant.DepositType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.math.BigDecimal

class BukkitCommandListener : CommandExecutor, TabCompleter, ICommand {
    private val mCommandsForCommon = mutableListOf(
        OVERVIEW.value, DEPOSIT.value, WITHDRAW.value, QUERY_ANNOUNCEMENT.value
    )

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>?
    ): Boolean = if (command.name.equals(COMMAND, true)) {
        execute(sender, args)
        true
    } else {
        false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> = getTabCommandList(sender, args)

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

    override fun CommandSender.overview() = sendMessage(createOverviewUI(this))

    override fun CommandSender.deposit(args: Array<out String>) {
        if (!isLegalAmount(this, args[2])) {
            return
        }
        sendMessage("${YELLOW}正在处理交易，请稍候……")
        when (args[1]) {
            DepositType.CURRENT.type.toString() -> Unit
            else -> sendMessage("${RED}所选存款类型不存在，请重新检查后输入！")
        }
    }

    override fun CommandSender.withdraw(args: Array<out String>) {
        if (!isLegalAmount(this, args[2])) {
            return
        }
        sendMessage("${YELLOW}正在处理交易，请稍候……")
        when (args[1]) {
            DepositType.CURRENT.type.toString() -> Unit
            // TODO
            else -> sendMessage("${RED}所选存单不存在，请重新检查后输入！")
        }
    }

    override fun CommandSender.queryAnnouncement() {
        TODO("Not yet implemented")
    }

    override fun CommandSender.publishAnnouncement(args: Array<out String>) {
        if (isOp) {
            // TODO
        } else {
            sendMessage("${RED}没有执行该指令的权限！")
        }
    }

    private fun isLegalAmount(sender: CommandSender, amount: String): Boolean {
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

    private fun createOverviewUI(sender: CommandSender): Component {
        return Component.text()
            .append(createTopOrBottomBar())
            .append(createNewLine())
            .append(Component.text("用户名：${sender.name}"))
            .append(createNewLine())
            .append(Component.text("账号：${if (sender is Player) sender.identity().uuid().toString() else ""}"))
            .append(createNewLine(2))
            .append(createPartLine("最新公告"))
            .append(
                Component.text(
                    """
                $GREEN[央行下调存款利率] @ 2024-07-28 16:19:00
                ${GREEN}央行下调存款利率，央行下调存款利率，央行下调存款利率，央行下调存款利率
            """.trimIndent()
                )
            )
            .append(createNewLine())
            .append(
                createClickButton(
                    "$DARK_AQUA>>>>${pluginLang.get()?.get("moreAnnouncements")}<<<<<"
                ) {
                    ClickEvent.runCommand("/ddbank help")
                }
            )
            .append(createNewLine(2))
            .append(createPartLine("储蓄情况"))
            .append(Component.text("$GREEN[活期存款] 1234567890.21  "))
            .append(createDepositOrWithdrawButton(sender, true))
            .append(createNewLine())
            .append(Component.text("$GREEN[定期存款] 1234567890.21  "))
            .append(createDepositOrWithdrawButton(sender, false))
            .append(createNewLine(2))
            .append(createTopOrBottomBar())
            .build()
    }

    private fun createDepositOrWithdrawButton(sender: CommandSender, isCurrent: Boolean): Component {
        return Component.text("$DARK_AQUA[存款]  ")
            .clickEvent(
                ClickEvent.callback {
                    if (isCurrent) {
                        sender.sendMessage("${RED}请使用指令 /ddbank deposit 0 <待存金额> 进行存款")
                    } else {
                        // TODO: 展示存款列表
                        sender.sendMessage("${RED}请使用指令 /ddbank deposit <存款产品编号> <待存金额> 进行存款")
                    }
                }
            ).append(
                Component.text("$DARK_AQUA[取款]")
                    .clickEvent(
                        ClickEvent.callback {
                            if (isCurrent) {
                                sender.sendMessage("${RED}请使用指令 /ddbank withdraw 0 <待取金额> 进行取款")
                            } else {
                                // TODO: 展示存单列表
                                sender.sendMessage("${RED}请使用指令 /ddbank withdraw <存单编号> <待取金额> 进行取款")
                            }
                        }
                    )
            )
    }

    private fun createTopOrBottomBar(): Component {
        return Component.text("$YELLOW========DripDrop在线银行========")
    }

    private fun createNewLine(amount: Int = 1): Component {
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

    private fun createPartLine(title: String): Component {
        return createNewLine()
            .append(Component.text("$RED--------$title--------"))
            .append(createNewLine())
    }

    private fun createClickButton(title: String, action: () -> Unit): Component {
        return Component.text(title).clickEvent(ClickEvent.callback { action() })
    }
}