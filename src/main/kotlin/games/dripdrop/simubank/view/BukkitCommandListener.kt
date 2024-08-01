package games.dripdrop.simubank.view

import games.dripdrop.simubank.controller.database.MySQLManager
import games.dripdrop.simubank.controller.interfaces.AbstractCommandManager
import games.dripdrop.simubank.controller.utils.*
import games.dripdrop.simubank.model.constant.DepositType
import games.dripdrop.simubank.model.constant.FileEnums
import games.dripdrop.simubank.model.data.Announcement
import games.dripdrop.simubank.model.data.Product
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

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
        MySQLManager.queryAnnouncementWithPaging {
            sendMessage(createAnnouncementList(it))
        }
    }

    override fun CommandSender.publishAnnouncement(args: Array<out String>) {
        if (isOp) {
            getSpecifiedYaml(currentPlugin, FileEnums.ANNOUNCEMENT) {
                pluginAnnouncement.set(it)
                sendMessage(
                    Component.text()
                        .append(Component.text("$GREEN["))
                        .append(Component.text(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())))
                        .append(Component.text(" ${GREEN}来自银行的公告]"))
                        .append(createNewLine())
                        .append(Component.text("${RED}${it.get("title") ?: ""}"))
                        .append(createNewLine())
                        .append(Component.text("${YELLOW}${it.get("content") ?: ""}"))
                        .build()
                )
            }
        } else {
            sendMessage("${RED}玩家 $name 没有执行该指令的权限！")
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

    private fun createAccountBar(sender: CommandSender): Component {
        return Component.text()
            .append(createNewLine())
            .append(Component.text("用户名：${sender.name}"))
            .append(createNewLine())
            .append(Component.text("账号：${if (sender is Player) sender.identity().uuid() else "----"}"))
            .append(createNewLine(2))
            .build()
    }

    private fun createAnnouncementPart(sender: CommandSender): Component {
        return Component.text()
            .append(createPartLine("最新公告"))
            .append(
                Component.text(
                    "$GREEN[ ${pluginAnnouncement.get()?.getString("title") ?: "暂无公告"} ]"
                )
            ).append(createNewLine())
            .append(Component.text(pluginAnnouncement.get()?.getString("content") ?: ""))
            .append(createNewLine())
            .append(
                createClickButton("$DARK_AQUA>>>>点此查看更多公告<<<<<") {
                    sender.queryAnnouncement()
                }
            )
            .append(createNewLine(2))
            .build()
    }

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

    private fun createDepositButton(sender: CommandSender, isCurrent: Boolean): Component {
        return Component.text()
            .content("$DARK_AQUA[存款]")
            .clickEvent(ClickEvent.callback {
                sender.sendMessage(
                    if (isCurrent) {
                        "${RED}请执行指令 /ddbank deposit 0 <待存金额> 进行存款"
                    } else {
                        createFixedDepositProductList(sender)
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

    private fun createFixedDepositProductList(sender: CommandSender) {
        getSpecifiedYaml(currentPlugin, FileEnums.PRODUCTS) {
            val component = Component.text()
                .append(Component.text("$RED========定期存款产品========"))
                .append(createNewLine())
            it.getMapList("products").onEach { product ->
                if ("C001" != product["productCode"]) {
                    gson.fromJson(gson.toJson(product), Product::class.java)?.let { p ->
                        component.append(Component.text("$YELLOW${p.productCode} "))
                            .append(
                                Component.text("$AQUA${p.name} ")
                                    .hoverEvent(HoverEvent.showText(Component.text(p.desc)))
                            )
                            .append(Component.text("${GREEN}${p.minimumAmount} 起存"))
                            .append(createNewLine())
                    }
                }
            }
            component.append(Component.text("$RED========定期存款产品========"))
            sender.sendMessage(component)
        }
    }

    private fun createDepositDataList() {
        // TODO
    }
}