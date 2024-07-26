package games.dripdrop.simubank.controller.utils


import games.dripdrop.simubank.DripDropBank
import games.dripdrop.simubank.model.constant.FileEnums
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

const val COMMAND = "ddbank"
private var mConfiguration: YamlConfiguration? = null
private val mLogger = LoggerFactory.getLogger(DripDropBank::class.java.simpleName)

fun runSyncTask(plugin: JavaPlugin, action: () -> Unit) {
    Bukkit.getScheduler().runTask(plugin, action)
}

fun runAsyncTask(plugin: JavaPlugin, action: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable { action() })
}

fun d(msg: String) = mLogger.debug(msg)

fun i(msg: String) = mLogger.info(msg)

fun w(msg: String) = mLogger.warn(msg)

fun e(msg: String) = mLogger.error(msg)

fun getPluginConfig(): YamlConfiguration? {
    i("is configuration null: ${mConfiguration == null}")
    return mConfiguration
}

fun getConfigYaml(plugin: JavaPlugin) {
    if (!checkFileOrDirectoryAvailable(plugin.dataFolder, FileEnums.CONFIG)) {
        return
    }
    try {
        val file = File("${plugin.dataFolder}${File.separator}${FileEnums.CONFIG.file}")
        i("config file path is [${file.path}]")
        mConfiguration = YamlConfiguration.loadConfiguration(file)
    } catch (e: Exception) {
        e("failed to get config yaml file: ${e.localizedMessage}")
        e.printStackTrace()
    }
}

fun checkFileOrDirectoryAvailable(directory: File, file: FileEnums): Boolean {
    i("checkFileOrDirectoryAvailable called: ${directory.path}")
    return try {
        if (!directory.exists()) {
            i("try to make directory [$directory]")
            directory.mkdir().apply { i("result = $this") }
        }
        i("directory [$directory] exists: ${directory.exists()}")
        val f = File(directory, file.file)
        i("file [$f] exists: ${f.exists()}")
        if (directory.exists() && !f.exists()) {
            DripDropBank::class.java.getResourceAsStream("/${file.file}")?.let {
                Files.copy(it, f.toPath()).apply { i("file copy result: $this") }
            }
        }
        f.exists()
    } catch (e: Exception) {
        e("failed to check or copy file: ${e.localizedMessage}")
        e.printStackTrace()
        false
    }
}