package games.dripdrop.simubank

import games.dripdrop.simubank.view.BankCommand
import games.dripdrop.simubank.view.EventListener
import net.md_5.bungee.api.plugin.Plugin

class SimuBank : Plugin() {
    private val mBankCommand = BankCommand()
    private val mEventListener = EventListener()

    override fun onEnable() {
        logger.info("SimuBack plugin is enabled")
        logger.info("register command listener")
        proxy.pluginManager.registerCommand(this, mBankCommand)
        logger.info("register event listener")
        proxy.pluginManager.registerListener(this, mEventListener)
    }

    override fun onDisable() {
        logger.info("SimuBack plugin is shutdown")
    }
}
