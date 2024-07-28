package games.dripdrop.simubank.view

import games.dripdrop.simubank.controller.utils.i
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class BukkitEventListener : Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerJoinEvent) {
        event.player.sendMessage(
            """
            ${ChatColor.GREEN}欢迎 ${event.player.name} 加入游戏，你可以通过 /ddbank 指令了解DripDrop在线银行！
        """.trimIndent()
        )
        // TODO
    }

    @EventHandler
    fun onPlayerLogout(event: PlayerQuitEvent) {
        i("player ${event.player.name} quited")
        // TODO
    }
}