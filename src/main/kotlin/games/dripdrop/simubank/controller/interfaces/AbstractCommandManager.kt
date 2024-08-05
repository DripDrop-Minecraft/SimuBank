package games.dripdrop.simubank.controller.interfaces

import games.dripdrop.simubank.controller.utils.*
import games.dripdrop.simubank.model.constant.Command.*
import games.dripdrop.simubank.model.constant.FileEnums
import games.dripdrop.simubank.model.data.Product
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

abstract class AbstractCommandManager : ICommand {
    private val mCommandsForCommon = mutableListOf(
        OVERVIEW.value, DEPOSIT.value, WITHDRAW.value, QUERY_ANNOUNCEMENT.value
    )
    protected val mAnnouncementIndexMap = hashMapOf<UUID, Int>()
    protected val mDepositIndexMap = hashMapOf<UUID, Int>()

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

    protected fun CommandSender.createFixedDepositProductList() {
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
            sendMessage(component)
        }
    }

    protected fun CommandSender.createAccountBar() {
        Component.text()
            .append(Component.text("用户名：$name"))
            .append(createNewLine())
            .append(Component.text("账号：${if (this is Player) identity().uuid() else "----"}"))
            .build().apply { sendMessage(this) }
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
        return Component.text("$RED--------$title--------")
            .append(createNewLine())
    }

    protected fun createClickButton(title: String, action: () -> Unit): Component {
        return Component.text(title).clickEvent(ClickEvent.callback { action() })
    }
}