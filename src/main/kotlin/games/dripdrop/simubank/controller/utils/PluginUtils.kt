package games.dripdrop.simubank.controller.utils


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import games.dripdrop.simubank.BukkitBankPlugin
import games.dripdrop.simubank.model.constant.FileEnums
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

const val CHANNEL_NAME = "BungeeCordPlugin"
const val COMMAND = "ddbank"
lateinit var currentPlugin: BukkitBankPlugin
var pluginAnnouncement = AtomicReference<YamlConfiguration>(null)
var pluginConfig = AtomicReference<YamlConfiguration?>(null)
var pluginLang = AtomicReference<YamlConfiguration?>(null)
val gson: Gson = GsonBuilder().setPrettyPrinting().setLenient()
    .serializeNulls()
    .disableHtmlEscaping()
    .create()

fun createDepositSN(playerUUID: String): String {
    return StringBuilder("DDB")
        .append(SimpleDateFormat("yyMMddHHmmss").format(Date()))
        .append(playerUUID.substring(0, 4).uppercase())
        .toString()
}

inline fun <reified T> ResultSet.getResultList(callback: (Collection<T?>) -> Unit) {
    val map = hashMapOf<String, Any?>()
    val list = arrayListOf<T?>()
    while (next()) {
        map.clear()
        T::class.java.declaredFields.onEach { field ->
            when (field.type) {
                String::class.java -> map[field.name] = getString(field.name)
                Int::class.java -> map[field.name] = getInt(field.name)
                Long::class.java -> map[field.name] = getLong(field.name)
                Double::class.java -> map[field.name] = getDouble(field.name)
                Boolean::class.java -> map[field.name] = getBoolean(field.name)
                else -> map[field.name] = null
            }
        }
        try {
            list.add(gson.fromJson(gson.toJson(map), T::class.java))
        } catch (e: Exception) {
            e("failed to convert result to a [${T::class.java.simpleName}] object: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
    callback(list)
}

fun runSyncTask(plugin: JavaPlugin, action: () -> Unit) {
    Bukkit.getScheduler().runTask(plugin, Runnable {
        i("current thread is: ${Thread.currentThread().name}")
        action()
    })
}

fun runAsyncTask(plugin: JavaPlugin, action: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
        i("current thread is: ${Thread.currentThread().name}")
        action()
    })
}

fun d(msg: String) = println(msg) // Bukkit.getLogger().log(Level.ALL, "[DripDropBank] $msg")

fun i(msg: String) = println(msg) // Bukkit.getLogger().log(Level.INFO, "[DripDropBank] $msg")

fun w(msg: String) = println(msg) // Bukkit.getLogger().log(Level.WARNING, "[DripDropBank] $msg")

fun e(msg: String) = println(msg) // Bukkit.getLogger().log(Level.SEVERE, "[DripDropBank] $msg")

fun getSpecifiedYaml(
    plugin: JavaPlugin,
    fileEnums: FileEnums,
    action: (YamlConfiguration) -> Unit = {}
) {
    runAsyncTask(plugin) {
        if (!checkFileOrDirectoryAvailable(plugin.dataFolder, fileEnums)) {
            return@runAsyncTask
        }
        try {
            val file = File("${plugin.dataFolder}${File.separator}${fileEnums.file}")
            YamlConfiguration.loadConfiguration(file).apply(action)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun loadYamlFiles(plugin: JavaPlugin, action: (YamlConfiguration) -> Unit) {
    i("start to load plugin yaml files...")
    getSpecifiedYaml(plugin, FileEnums.CONFIG) {
        pluginConfig.set(it)
        action(it)
    }
    getSpecifiedYaml(plugin, FileEnums.LANG) {
        pluginLang.set(it)
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
            BukkitBankPlugin::class.java.getResourceAsStream("/${file.file}")?.let {
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