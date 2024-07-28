package games.dripdrop.simubank

import games.dripdrop.simubank.controller.utils.CHANNEL_NAME
import games.dripdrop.simubank.view.BungeeCordCommandListener
import games.dripdrop.simubank.view.BungeeCordEventListener
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordBankPlugin : Plugin() {
    private val mBungeeCordCommandListener = BungeeCordCommandListener()
    private val mBungeeCordEventListener = BungeeCordEventListener(this)

    override fun onEnable() {
        logger.info("BungeeCord plugin BungeeCordPlugin is enabled")
        logger.info("register command listener")
        proxy.pluginManager.registerCommand(this, mBungeeCordCommandListener)
        logger.info("register event listener")
        proxy.pluginManager.registerListener(this, mBungeeCordEventListener)
        logger.info("register channel")
        proxy.registerChannel(CHANNEL_NAME)
    }

    override fun onDisable() {
        proxy.pluginManager.unregisterCommands(this)
        proxy.pluginManager.unregisterListeners(this)
        proxy.unregisterChannel(CHANNEL_NAME)
        logger.info("BungeeCord plugin BungeeCordPlugin is shutdown")
    }
}
