package games.dripdrop.simubank.controller.interfaces

import org.bukkit.command.CommandSender

interface ICommand {
    // 指令输入执行
    fun execute(sender: CommandSender, args: Array<out String>?)

    // 获取tab指令列表
    fun getTabCommandList(sender: CommandSender, args: Array<out String>?): MutableList<String>

    // 查询概况
    fun CommandSender.overview()

    // 存款
    fun CommandSender.deposit(args: Array<out String>)

    // 取款
    fun CommandSender.withdraw(args: Array<out String>)

    // 查询公告
    fun CommandSender.queryAnnouncement()

    // 发布公告
    fun CommandSender.publishAnnouncement(args: Array<out String>)
}