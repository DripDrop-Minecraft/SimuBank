package games.dripdrop.simubank

import net.md_5.bungee.api.plugin.Plugin

class SimuBank : Plugin() {

    override fun onEnable() {
        logger.info("SimuBack plugin is enabled")
    }

    override fun onDisable() {
        logger.info("SimuBack plugin is shutdown")
    }
}
