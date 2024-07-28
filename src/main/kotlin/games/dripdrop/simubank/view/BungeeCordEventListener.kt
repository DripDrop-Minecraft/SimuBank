package games.dripdrop.simubank.view

import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

class BungeeCordEventListener(private val plugin: Plugin) : Listener {

    @EventHandler
    fun onPlayerLogin(event: PostLoginEvent) {
        // TODO
        event.player.uniqueId
    }

    @EventHandler
    fun onPlayerLogout(event: PlayerDisconnectEvent) {
        //TODO
        event.player.uniqueId
    }
}