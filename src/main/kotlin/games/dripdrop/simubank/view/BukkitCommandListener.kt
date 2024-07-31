package games.dripdrop.simubank.view

import games.dripdrop.simubank.controller.database.MySQLManager
import games.dripdrop.simubank.controller.interfaces.AbstractCommandManager
import games.dripdrop.simubank.controller.utils.COMMAND
import games.dripdrop.simubank.controller.utils.currentPlugin
import games.dripdrop.simubank.controller.utils.getSpecifiedYaml
import games.dripdrop.simubank.controller.utils.pluginAnnouncement
import games.dripdrop.simubank.model.constant.DepositType
import games.dripdrop.simubank.model.constant.FileEnums
import games.dripdrop.simubank.model.data.Announcement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class BukkitCommandListener : CommandExecutor, TabCompleter, AbstractCommandManager() {

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
        MySQLManager.queryAnnouncementWithPaging {
            createAnnouncementList(it)
        }
    }

    override fun CommandSender.publishAnnouncement(args: Array<out String>) {
        if (isOp) {
            // TODO
        } else {
            sendMessage("${RED}没有执行该指令的权限！")
        }
    }

    private fun createOverviewUI(sender: CommandSender): Component {
        return Component.text()
            .append(createTopOrBottomBar())
            .append(createAccountBar(sender))
            .append(createAnnouncementPart(sender))
            .append(createBalancePart(sender))
            .append(createTopOrBottomBar())
            .build()
    }

    private fun createBalancePart(sender: CommandSender): Component {
        return Component.text()
            .append(createPartLine("储蓄情况"))
            .append(Component.text("$GREEN[活期存款] 1234567890.21  "))
            .append(createDepositOrWithdrawButton(sender, true))
            .append(createNewLine())
            .append(Component.text("$GREEN[定期存款] 1234567890.21  "))
            .append(createDepositOrWithdrawButton(sender, false))
            .append(createNewLine(2))
            .build()
    }

    private fun createAnnouncementPart(sender: CommandSender): Component {
        getSpecifiedYaml(currentPlugin, FileEnums.ANNOUNCEMENT) {
            pluginAnnouncement.set(it)
        }
        return Component.text()
            .append(createPartLine("最新公告"))
            .append(
                Component.text(
                    """
                $GREEN[${pluginAnnouncement.get()?.getString("title")}]
                ${pluginAnnouncement.get()?.getString("content")}
            """.trimIndent()
                )
            )
            .append(createNewLine())
            .append(
                createClickButton(
                    "$DARK_AQUA>>>>点此查看更多公告<<<<<"
                ) {
                    sender.queryAnnouncement()
                }
            )
            .append(createNewLine(2))
            .build()
    }

    private fun createAnnouncementList(announcements: Collection<Announcement?>): Component {
        return Component.text("${YELLOW}========近期公告========")
            .apply {
                announcements.onEach { a ->
                    a?.let { append(createAnnouncementItem(it)) }
                }
            }
            .append(createNewLine())
            .append(Component.text("${YELLOW}======== "))
            .append(Component.text("${RED}上一页").clickEvent(ClickEvent.callback {
                // TODO
            }))
            .append(Component.text(" "))
            .append(Component.text("${GREEN}下一页").clickEvent(ClickEvent.callback {
                // TODO
            }))
            .append(Component.text("$YELLOW ========"))
    }

    private fun createAnnouncementItem(announcement: Announcement): Component {
        return Component.text()
            .append(Component.text("$RED${announcement.timestamp} "))
            .append(
                Component.text("$GREEN${announcement.title}").hoverEvent(
                    HoverEvent.showText(Component.text(announcement.content))
                )
            )
            .append(createNewLine())
            .build()
    }

    private fun createAccountBar(sender: CommandSender): Component {
        return Component.text()
            .append(createNewLine())
            .append(Component.text("用户名：${sender.name}"))
            .append(createNewLine())
            .append(Component.text("账号：${if (sender is Player) sender.identity().uuid() else "----"}"))
            .append(createNewLine(2))
            .build()
    }

    private fun createDepositOrWithdrawButton(sender: CommandSender, isCurrent: Boolean): Component {
        return Component.text("$DARK_AQUA[存款]  ")
            .clickEvent(
                ClickEvent.callback {
                    if (isCurrent) {
                        ClickEvent.suggestCommand("/ddbank deposit 0 <待存金额>")
                        sender.sendMessage("${RED}请输入待存金额并执行指令进行存款")
                    } else {
                        createFixedDepositProductList()
                        ClickEvent.suggestCommand("/ddbank deposit <存款产品编号> <待存金额>")
                        sender.sendMessage("${RED}请输入存款产品编号和待存金额并执行指令进行存款")
                    }
                }
            ).append(
                Component.text("$DARK_AQUA[取款]")
                    .clickEvent(
                        ClickEvent.callback {
                            if (isCurrent) {
                                ClickEvent.suggestCommand("/ddbank withdraw 0 <待取金额>")
                                sender.sendMessage("${RED}请输入待存金额并执行指令进行取款")
                            } else {
                                createDepositDataList()
                                ClickEvent.suggestCommand("/ddbank withdraw <存单编号> <待取金额>")
                                sender.sendMessage("${RED}请输入存单编号和待取金额并执行指令进行取款")
                            }
                        }
                    )
            )
    }

    private fun createFixedDepositProductList() {
        // TODO
    }

    private fun createDepositDataList() {
        // TODO
    }
}