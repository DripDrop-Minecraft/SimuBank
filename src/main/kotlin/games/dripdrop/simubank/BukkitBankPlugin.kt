package games.dripdrop.simubank

import games.dripdrop.simubank.controller.database.MySQLManager
import games.dripdrop.simubank.controller.utils.COMMAND
import games.dripdrop.simubank.controller.utils.currentPlugin
import games.dripdrop.simubank.controller.utils.i
import games.dripdrop.simubank.controller.utils.loadYamlFiles
import games.dripdrop.simubank.view.BukkitCommandListener
import games.dripdrop.simubank.view.BukkitEventListener
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class BukkitBankPlugin : JavaPlugin() {
    private val mBukkitCommandListener = BukkitCommandListener()
    private val mBukkitEventListener = BukkitEventListener()

    override fun onEnable() {
        i("${this::class.java.simpleName} is enabled now!")
        currentPlugin = this
        registerListeners()
        loadYamlFiles(this) { MySQLManager.initMySQLDatabase(it) }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(mBukkitEventListener)
        i("${this::class.java.simpleName} is shutdown now!")
    }

    private fun registerListeners() {
        i("register event listener...")
        server.pluginManager.registerEvents(mBukkitEventListener, this)
        getCommand(COMMAND)?.apply {
            i("register command listener...")
            setExecutor(mBukkitCommandListener)
            tabCompleter = mBukkitCommandListener
        }
    }
}
