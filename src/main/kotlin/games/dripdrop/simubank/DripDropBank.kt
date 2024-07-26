package games.dripdrop.simubank

import com.google.common.io.ByteStreams
import games.dripdrop.simubank.controller.database.MySQLManager
import games.dripdrop.simubank.controller.utils.getConfigYaml
import games.dripdrop.simubank.controller.utils.i
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener

class DripDropBank : JavaPlugin(), PluginMessageListener {
    private val mChannelName = "BungeeCord"

    override fun onEnable() {
        i("${this::class.java.simpleName} is enabled now!")
        i("register outgoing plugin channel...")
        server.messenger.registerOutgoingPluginChannel(this, mChannelName)
        i("register incoming plugin channel...")
        server.messenger.registerIncomingPluginChannel(this, mChannelName, this)
        i("init plugin config...")
        getConfigYaml(this)
        MySQLManager.initDatabase()
    }

    override fun onDisable() {
        i("unregister outgoing plugin channel...")
        server.messenger.unregisterOutgoingPluginChannel(this, mChannelName)
        i("unregister outgoing plugin channel...")
        server.messenger.unregisterIncomingPluginChannel(this, mChannelName)
        i("${this::class.java.simpleName} is shutdown now!")
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray?) {
        if (mChannelName != channel) {
            return
        }
        ByteStreams.newDataInput(message).readUTF().apply {
            // TODO
        }
    }
}
