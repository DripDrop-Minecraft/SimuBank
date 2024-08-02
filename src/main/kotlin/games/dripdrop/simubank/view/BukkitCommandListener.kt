package games.dripdrop.simubank.view

import games.dripdrop.simubank.controller.database.MySQLManager
import games.dripdrop.simubank.controller.interfaces.AbstractCommandManager
import games.dripdrop.simubank.controller.utils.*
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

    override fun CommandSender.overview() {
        getSpecifiedYaml(currentPlugin, FileEnums.ANNOUNCEMENT) {
            pluginAnnouncement.set(it)
            sendMessage(createOverviewUI(this))
        }
    }

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
            else -> sendMessage("${RED}所选存单不存在，请重新检查后输入！")
        }
    }

    override fun CommandSender.queryAnnouncement() {
        if (this is Player) {
            queryAnnouncementWithPaging(this, 0)
        }
    }

    /*=============================创建首页=========================*/
    private fun createOverviewUI(sender: CommandSender): Component {
        return Component.text()
            .append(createTopOrBottomBar())
            .append(sender.createAccountBar())
            .append(createAnnouncementPart(sender))
            .append(createBalancePart(sender))
            .append(createTopOrBottomBar())
            .build()
    }

    /*=============================创建公告模块=========================*/
    private fun createAnnouncementPart(sender: CommandSender): Component {
        val announcement = pluginAnnouncement.get()
        return Component.text()
            .append(createPartLine("最新公告"))
            .append(Component.text("$GREEN[ ${announcement?.getString("title") ?: "暂无公告"} ]"))
            .append(createNewLine())
            .append(Component.text(announcement?.getString("content") ?: ""))
            .append(createNewLine())
            .append(createClickButton("$DARK_AQUA>>>>点此查看更多公告<<<<<") {
                sender.queryAnnouncement()
            })
            .append(createNewLine(2))
            .build()
    }

    /*=============================创建近期公告列表模块=========================*/
    private fun queryAnnouncementWithPaging(player: Player, startPosition: Int) {
        runAsyncTask(currentPlugin) {
            MySQLManager.queryAnnouncementWithPaging(offset = startPosition) {
                if (!it.isEmpty()) {
                    mAnnouncementIndexMap[player.identity().uuid()] = startPosition
                    createAnnouncementList(player, it)
                } else {
                    player.sendMessage("${RED}已没有更多公告！")
                }
            }
        }
    }

    private fun createAnnouncementList(player: Player, announcements: Collection<Announcement?>) {
        player.sendMessage("")
        player.sendMessage(Component.text("${YELLOW}========更多公告========"))
        announcements.onEach {
            player.sendMessage(createAnnouncementItem(it))
        }
        Component.text("${GREEN}下一页")
            .clickEvent(createClickCallback(player))
            .append(createNewLine())
            .append(Component.text("$YELLOW========================="))
            .apply { player.sendMessage(this) }
        player.sendMessage("")
    }

    private fun createAnnouncementItem(announcement: Announcement?): Component {
        return if (announcement != null) {
            Component.text()
                .content("$RED[${announcement.timestamp}] $GREEN${announcement.title}")
                .hoverEvent(HoverEvent.showText(Component.text(announcement.content)))
                .build()
        } else {
            Component.text("")
        }
    }

    private fun createClickCallback(player: Player): ClickEvent {
        val currentPosition = (mAnnouncementIndexMap[player.identity().uuid()] ?: 0) + 5
        return ClickEvent.callback {
            queryAnnouncementWithPaging(player, currentPosition)
        }
    }

    /*=============================创建余额信息展示模块=========================*/
    private fun createBalancePart(sender: CommandSender): Component {
        return Component.text()
            .append(createPartLine("储蓄情况"))
            .append(Component.text("$GREEN[活期存款] 1234567890.21  "))
            .append(createDepositButton(sender, true))
            .append(createWithdrawButton(sender, true))
            .append(createNewLine())
            .append(Component.text("$GREEN[定期存款] 1234567890.21  "))
            .append(createDepositButton(sender, false))
            .append(createWithdrawButton(sender, false))
            .append(createNewLine(2))
            .build()
    }

    private fun createDepositButton(sender: CommandSender, isCurrent: Boolean): Component {
        return Component.text()
            .content("$DARK_AQUA[存款]")
            .clickEvent(ClickEvent.callback {
                sender.sendMessage(
                    if (isCurrent) {
                        "${RED}请执行指令 /ddbank deposit 0 <待存金额> 进行存款"
                    } else {
                        sender.createFixedDepositProductList()
                        "${RED}请执行指令 /ddbank deposit <存款产品编号> <待存金额> 进行存款"
                    }
                )
            })
            .append(Component.text("  "))
            .build()
    }

    private fun createWithdrawButton(sender: CommandSender, isCurrent: Boolean): Component {
        return Component.text()
            .content("$DARK_AQUA[取款]")
            .clickEvent(ClickEvent.callback {
                sender.sendMessage(
                    if (isCurrent) {
                        "${RED}请执行指令 /ddbank withdraw 0 <待取金额> 进行取款"
                    } else {
                        createDepositDataList()
                        "${RED}请执行指令 /ddbank withdraw <存单编号> <待取金额> 进行取款"
                    }
                )
            })
            .build()
    }

    private fun createDepositDataList() {
        // TODO
    }
}