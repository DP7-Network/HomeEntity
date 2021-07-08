@file:JvmName("HomeEntity")
package cn.thelama.homeent

import cn.thelama.homeent.back.BackHandler
import cn.thelama.homeent.exit.ExitHandler
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.notice.Notice
import cn.thelama.homeent.p.PrivateHandler
import cn.thelama.homeent.relay.Relay
import cn.thelama.homeent.relay.RelayBotV1
import cn.thelama.homeent.relay.RelayBotHandler
import cn.thelama.homeent.relay.RelayBotV2
import cn.thelama.homeent.secure.SecureHandler
import cn.thelama.homeent.show.ShowCompleter
import cn.thelama.homeent.show.ShowHandler
import cn.thelama.homeent.show.ShowManager
import cn.thelama.homeent.slime.SlimeHandler
import cn.thelama.homeent.tpa.*
import cn.thelama.homeent.warp.HomeHandler
import cn.thelama.homeent.warp.WarpCompleter
import cn.thelama.homeent.warp.WarpHandlerV2
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Hex
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import sun.misc.Unsafe
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.security.MessageDigest
import java.util.*
import kotlin.system.measureTimeMillis
import pw.yumc.Yum.api.YumAPI

class HomeEntity : JavaPlugin(), Listener {
    companion object {
        const val VERSION = "1.6 Pre-Release"
        const val JENKINS_BASE = "https://ci.thelama.cn"
        lateinit var instance: HomeEntity
        lateinit var COMMIT_HASH: String
        lateinit var BRANCH: String
        var BUILD_NUMBER: Int = 0
    }
    private val gson = Gson()

    lateinit var botInstance: Relay
    lateinit var minecraftTranslation: HashMap<String, String>
    lateinit var globalNetworkProxy: Proxy
    lateinit var httpClient: OkHttpClient
    val lastTeleport: HashMap<UUID, Location> = HashMap()
    val commandHelp: Array<BaseComponent> = ComponentBuilder("${ChatColor.GOLD}指令参数错误! ")
        .append(ComponentBuilder(
        "${ChatColor.GOLD}» ${ChatColor.UNDERLINE}点击这里获取帮助${ChatColor.RESET}${ChatColor.GOLD} «")
        .event(ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/DP7-Network/HomeEntity"))
        .create()).create()

    override fun onEnable() {
        instance = this
        runCatching {
            Properties().also {
                it.load(this::class.java.classLoader.getResourceAsStream("BUILD.INFO"))
                BRANCH = it.getProperty("BRANCH").replace("origin/", "")
                COMMIT_HASH = it.getProperty("HASH")
                BUILD_NUMBER = it.getProperty("BUILD").toInt()
                if(it.contains("UNSTABLE")) {
                    Bukkit.getLogger().warning("You are using a **unstable** build!")
                }
            }
        }.onFailure {
            Bukkit.getLogger().warning("Failed to get version info of this plugin!!")
            BRANCH = "Unknown"
            COMMIT_HASH = ""
        }

        measureTimeMillis {
            logger.info("")
            logger.info("")
            logger.info("")
            logger.runCatching {
                info("${ChatColor.GREEN}Welcome to HomeEntity $VERSION ($BRANCH@${COMMIT_HASH.substring(0, 7)})")
            }.onFailure {
                logger.info("${ChatColor.GREEN}Welcome to HomeEntity $VERSION (???@???")
            }
            if(!dataFolder.exists()) {
                dataFolder.mkdir()
            }

            measureTimeMillis {
                ModuledPlayerDataManager.init(dataFolder)
            }.also {
                println("Player data loaded in ${it}ms")
            }

            if(config.getBoolean("proxy.enable")) {
                if(config.getString("proxy.type")?.toLowerCase() == "http") {
                    this.globalNetworkProxy = Proxy(Proxy.Type.HTTP,
                        InetSocketAddress(config.getString("proxy.ip"), config.getInt("proxy.port")))
                } else if (config.getString("proxy.type")?.toLowerCase() == "socks") {
                    this.globalNetworkProxy = Proxy(Proxy.Type.SOCKS,
                        InetSocketAddress(config.getString("proxy.ip"), config.getInt("proxy.port")))
                } else {
                    this.globalNetworkProxy = Proxy.NO_PROXY
                }
            } else {
                this.globalNetworkProxy = Proxy.NO_PROXY
            }

            logger.info("  Register commands...")

            this.getCommand("warp")!!.apply {
                setExecutor(WarpHandlerV2)
                tabCompleter = WarpCompleter
                logger.info("    ${ChatColor.GREEN}Command warp registered successfully")
            }

            this.getCommand("show")!!.apply {
                setExecutor(ShowHandler)
                tabCompleter = ShowCompleter
                logger.info("    ${ChatColor.GREEN}Command show registered successfully")
            }

            this.getCommand("back")!!.apply {
                setExecutor(BackHandler)
                logger.info("    ${ChatColor.GREEN}Command back registered successfully")
            }

            this.getCommand("session")!!.apply {
                setExecutor(SecureHandler)
                logger.info("    ${ChatColor.GREEN}Command session registered successfully")
            }

            this.getCommand("exit")!!.apply {
                setExecutor(ExitHandler)
                logger.info("    ${ChatColor.GREEN}Command exit registered successfully")
            }

            this.getCommand("slime")!!.apply {
                setExecutor(SlimeHandler)
                logger.info("    ${ChatColor.GREEN}Command slime registered successfully")
            }

            this.getCommand("tpa")!!.apply {
                setExecutor(TPAHandler)
                tabCompleter = TPACompleter
                logger.info("    ${ChatColor.GREEN}Command tpa registered successfully")
            }

            this.getCommand("tphere")!!.apply {
                setExecutor(TPHereHandler)
                tabCompleter = TPACompleter
                logger.info("    ${ChatColor.GREEN}Command tphere registered successfully")
            }

            this.getCommand("tpaccept")!!.apply {
                setExecutor(TPAcceptHandler)
                logger.info("    ${ChatColor.GREEN}Command tpaccept registered successfully")
            }

            this.getCommand("tpdeny")!!.apply {
                setExecutor(TPDenyHandler)
                logger.info("    ${ChatColor.GREEN}Command tpdeny registered successfully")
            }

            this.getCommand("relay")!!.apply {
                setExecutor(RelayBotHandler)
                logger.info("    ${ChatColor.GREEN}Command relay registered successfully")

            }

            this.getCommand("home")!!.apply {
                setExecutor(HomeHandler)
                logger.info("    ${ChatColor.GREEN}Command home registered successfully")
            }

            this.getCommand("sethome")!!.apply {
                setExecutor(HomeHandler)
                logger.info("    ${ChatColor.GREEN}Command sethome registered successfully")
            }

            logger.info("  Register events...")

            server.pluginManager.registerEvents(this, this)
            server.pluginManager.registerEvents(PrivateHandler, this)
            server.pluginManager.registerEvents(ShowManager, this)

            logger.info("  Finalizing...")

            server.onlinePlayers.forEach {
                it.setDisplayName(
                    "${ChatColor.AQUA}[${parseWorld(it.location.world?.name)}${ChatColor.AQUA}] ${it.name}")
            }
            //launchCheckUpdatesTask()
            logger.info("${ChatColor.RED}因为lama穷导致CI没钱续费 :( 自动更新无了")
            logger.info("${ChatColor.GREEN}Reached goal 'initialize'")
            logger.info("Launching Relay Bot")
            botInstance = if(config.getBoolean("relay.v2")) {
                logger.info("  Launching Relay bot v2")
                RelayBotV2(config.getLong("relay.listen"), config.getString("relay.token")!!)
            } else {
                logger.info("  Launching Relay bot v1")
                logger.warning("  You are using a deprecated feature!")
                RelayBotV1(config.getLong("relay.listen"), config.getString("relay.token")!!)
            }
            logger.info("${ChatColor.GREEN}Reached goal 'relay'")
        }.also {
            logger.info("${ChatColor.GREEN}HomeEntity Initialized Complete in $it ms")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisable() {
        WarpHandlerV2.save()
        SecureHandler.save()

        ModuledPlayerDataManager.save(this.dataFolder)
        GlobalScope.launch {
            botInstance.shutdown()
        }
        logger.info("${ChatColor.RED}Reached goal 'shutdown'")
    }

    private fun writeFile(file: File, data: String) {
        file.delete()
        file.createNewFile()
        val fw = FileWriter(file)
        fw.write(data)
        fw.flush()
        fw.close()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(command.name == "hent") {
            if(args.isNotEmpty()) {
                if(sender is ConsoleCommandSender) {
                    when(args[0]) {
                        "crash" -> {
                            Bukkit.getWorlds().forEach { it.save() }
                            val f: Field = Unsafe::class.java.getDeclaredField("theUnsafe").apply {
                                isAccessible = true
                            }
                            (f.get(null) as Unsafe).putAddress(0, 0)
                        }

                        "sync" -> {
                            if(args.size >= 2) {
                                tryUpdate(args[1], true)
                            } else {
                                sender.sendMessage("/hent sync <UpdateStream(HomeEntity|HomeEntity-Devel)>")
                            }
                        }
                    }
                }
            } else {
                sender.sendMessage("${ChatColor.AQUA}HomeEntity " +
                        "${ChatColor.RESET}- ${ChatColor.GREEN}$VERSION " +
                        "${ChatColor.RESET}| ${ChatColor.ITALIC}" +
                        "${ChatColor.YELLOW}Build $BUILD_NUMBER " +
                        "$BRANCH@${COMMIT_HASH.substring(7)}")
            }
        }
        return true
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        e.quitMessage =
            "${ChatColor.GRAY}[${ChatColor.RED}-${ChatColor.GRAY}] ${ChatColor.GRAY}${e.player.name}"
        botInstance.say("[-] ${e.player.name}")
    }
    
    @EventHandler
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if(e.from.world != e.to?.world) {
            e.player.setDisplayName(
                "${ChatColor.AQUA}[${parseWorld(e.to?.world?.name)}${ChatColor.AQUA}] ${e.player.name}")
        }
        lastTeleport[e.player.uniqueId] = e.from
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        lastTeleport[e.entity.uniqueId] = e.entity.location
    }

    private fun parseWorld(name: String?): String {
        if(name == null) {
            return "Void"
        }
        return when(name) {
            "world" -> {
                 "${ChatColor.GREEN}主世界"
            }

            "world_nether" -> {
                "${ChatColor.DARK_RED}下界"
            }

            "world_the_end" -> {
                "${ChatColor.LIGHT_PURPLE}末地"
            }

            else -> {
                "${ChatColor.BLUE}$name"
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        SecureHandler.setLoginState(e.player.uniqueId, false)
        e.joinMessage = "${ChatColor.GRAY}[${ChatColor.GREEN}+${ChatColor.GRAY}] ${ChatColor.GRAY}${e.player.name}"
        e.player.sendMessage("${ChatColor.GRAY}============================")
        e.player.sendMessage("${ChatColor.GOLD}  欢迎来到${config.getString("main.serverName")}      ")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'.l <密码>'       来登录")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'.r <密码> <密码>' 来注册")
        e.player.sendMessage("${ChatColor.RED}  若忘记密码请找在线管理员重置")
        e.player.sendMessage("${ChatColor.GRAY}============================")
        SecureHandler.limit(e.player)

        Bukkit.getScheduler().runTaskLater(this, Runnable {
            if(SecureHandler.getLoginState(e.player.uniqueId)) {
                SecureHandler.removeLimit(e.player)
            } else {
                SecureHandler.setLoginState(e.player.uniqueId, false)
                e.player.kickPlayer("${ChatColor.RED}登录验证超时")
            }
        }, 30 * 20)

        botInstance.say("[+] ${e.player.name}")
    }

    @EventHandler
    fun onPlayerDamage(e: EntityDamageByEntityEvent) {
        if(e.entity is Player && SecureHandler.getLoginState(e.entity.uniqueId)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        e.format =
            "${ChatColor.AQUA}[${ChatColor.RESET}${parseWorld(e.player.location.world?.name)}${ChatColor.AQUA}] " +
                    "${ChatColor.YELLOW}${e.player.name}${ChatColor.RESET}: " +
                    "${ChatColor.RESET}%2\$s"

        Notice.parseMessage(e.message).forEach {
            it.sendTitle("${ChatColor.YELLOW}有人提到你",
                "${ChatColor.YELLOW}${e.player.name}${ChatColor.WHITE} 在聊天消息中提到了你，快去看看",
                10, 3 * 20, 10)
        }

        if(!e.isCancelled && !RelayBotHandler.isDisabled(e.player.uniqueId)) {
            botInstance.say(e.player.name, e.message)
        }
    }

    fun sha256(str: String): String {
        return String(Hex.encodeHex(MessageDigest.getInstance("SHA-256")
            .digest(str.toByteArray(charset("UTF-8"))), false))
    }

    private fun launchCheckUpdatesTask() {
        val proj = when(BRANCH) {
            "master" -> {
                "HomeEntity"
            }

            "devel" -> {
                "HomeEntity-devel"
            }

            else -> {
                return
            }
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            tryUpdate(proj)
        }, 0, 3600 * 20)
    }

    private fun tryUpdate(stream: String, sync: Boolean = false) {
        val req = Request.Builder().url("$JENKINS_BASE/job/$stream/lastSuccessfulBuild/api/json").get().build()
        val rep = httpClient.newCall(req).execute().body?.string()
        if(rep == null) {
            return
        } else {
            val jsonTree = gson.fromJson(rep, JsonElement::class.java).asJsonObject
            if(jsonTree["number"].asInt > BUILD_NUMBER || sync) {
                Bukkit.broadcastMessage("${ChatColor.GREEN}HomeEntity: 可用更新已找到, 准备更新!")
                FileUtils.copyToFile(
                    URL("http://s1.lama3l9r.net/job/$stream" +
                            "/lastSuccessfulBuild/artifact/build/libs/HomeEntity-1.0-SNAPSHOT-all.jar")
                    .openConnection(
                    this.globalNetworkProxy).getInputStream(), File(Bukkit.getUpdateFolderFile(), this.file.name))
                Bukkit.broadcastMessage("更新已下载完毕！准备重载")
                YumAPI.reload(this)
            }
        }
    }
}
