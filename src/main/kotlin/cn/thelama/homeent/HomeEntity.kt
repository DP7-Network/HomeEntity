@file:JvmName("HomeEntity")
package cn.thelama.homeent

import cn.thelama.homeent.back.BackHandler
import cn.thelama.homeent.exit.ExitHandler
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.motd.MotdManager
import cn.thelama.homeent.mylovelycat.MyLovelyCat
import cn.thelama.homeent.notice.Notice
import cn.thelama.homeent.p.PrivateHandler
import cn.thelama.homeent.prefix.PrefixManager
import cn.thelama.homeent.relay.Relay
import cn.thelama.homeent.relay.RelayBotHandler
import cn.thelama.homeent.relay.RelayBotV2
import cn.thelama.homeent.secure.AdminHandler
import cn.thelama.homeent.secure.AuthHandler
import cn.thelama.homeent.show.ShowCompleter
import cn.thelama.homeent.show.ShowHandler
import cn.thelama.homeent.show.ShowManager
import cn.thelama.homeent.slime.SlimeHandler
import cn.thelama.homeent.tpa.*
import cn.thelama.homeent.warp.HomeHandler
import cn.thelama.homeent.warp.LocationEntry
import cn.thelama.homeent.warp.WarpCompleter
import cn.thelama.homeent.warp.WarpHandlerV2
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.minecraft.server.dedicated.DedicatedServer
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Hex
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.Yaml
import sun.misc.Unsafe
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.MessageDigest
import java.util.*
import kotlin.system.measureTimeMillis
import java.io.FileReader

class HomeEntity : JavaPlugin(), Listener {
    companion object {
        const val VERSION = "1.6 Pre-Release"
        lateinit var instance: HomeEntity
        lateinit var COMMIT_HASH: String
        lateinit var BRANCH: String
        var BUILD_NUMBER: Int = 0
        val theServer = run {
            val server = Bukkit.getServer() as CraftServer
            val dedicatedServerField = server.javaClass.getDeclaredField("console")
            dedicatedServerField.isAccessible = true
            dedicatedServerField.get(server) as DedicatedServer
        }
    }
    lateinit var botInstance: Relay
    lateinit var minecraftTranslation: HashMap<String, String>
    lateinit var globalNetworkProxy: Proxy
    val lastTeleport: HashMap<UUID, Location> = HashMap()
    val commandHelp: Array<BaseComponent> = ComponentBuilder("${ChatColor.GOLD}指令参数错误! ")
        .append(ComponentBuilder(
        "${ChatColor.GOLD}» ${ChatColor.UNDERLINE}点击这里获取帮助${ChatColor.RESET}${ChatColor.GOLD} «")
        .event(ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/DP7-Network/HomeEntity"))
        .create()).create()

    override fun onLoad() {
        logger.info("预加载...")
        logger.info(LocationEntry().toString())
    }

    override fun onEnable() {
        instance = this
        runCatching {
            Properties().also {
                it.load(this::class.java.classLoader.getResourceAsStream("BUILD.INFO"))
                BRANCH = it.getProperty("BRANCH").replace("origin/", "")
                COMMIT_HASH = it.getProperty("HASH")
                BUILD_NUMBER = it.getProperty("BUILD").toInt()
                if(it.contains("UNSTABLE")) {
                    Bukkit.getLogger().warning("你正在使用**不稳定**的版本!!!")
                }
            }
        }.onFailure {
            Bukkit.getLogger().warning("无法获取插件版本信息!!")
            BRANCH = "Unknown"
            COMMIT_HASH = ""
        }

        measureTimeMillis {
            logger.runCatching {
                info("${ChatColor.GREEN}欢迎使用 HomeEntity $VERSION ($BRANCH@${COMMIT_HASH.substring(0, 7)})")
            }.onFailure {
                logger.info("${ChatColor.GREEN}欢迎使用 HomeEntity $VERSION (???@???")
            }
            if(!dataFolder.exists()) {
                dataFolder.mkdir()
            }

            measureTimeMillis {
                ModuledPlayerDataManager.init(this.config)
            }.also {
                println("加载玩家数据用时 ${it}ms")
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

            logger.info("注册指令即事件监听器中...")

            this.getCommand("warp")!!.apply {
                setExecutor(WarpHandlerV2)
                tabCompleter = WarpCompleter
            }

            this.getCommand("prefix")!!.apply {
                setExecutor(PrefixManager)
            }

            this.getCommand("show")!!.apply {
                setExecutor(ShowHandler)
                tabCompleter = ShowCompleter
            }

            this.getCommand("back")!!.apply {
                setExecutor(BackHandler)
            }

            this.getCommand("auth")!!.apply {
                setExecutor(AuthHandler)
            }

            this.getCommand("admin")!!.apply {
                setExecutor(AdminHandler)
            }

            this.getCommand("exit")!!.apply {
                setExecutor(ExitHandler)
            }

            this.getCommand("slime")!!.apply {
                setExecutor(SlimeHandler)
            }

            this.getCommand("tpa")!!.apply {
                setExecutor(TPAHandler)
                tabCompleter = TPACompleter
            }

            this.getCommand("tphere")!!.apply {
                setExecutor(TPHereHandler)
                tabCompleter = TPACompleter
            }

            this.getCommand("tpaccept")!!.apply {
                setExecutor(TPAcceptHandler)
            }

            this.getCommand("tpdeny")!!.apply {
                setExecutor(TPDenyHandler)
            }

            this.getCommand("relay")!!.apply {
                setExecutor(RelayBotHandler)
            }

            this.getCommand("home")!!.apply {
                setExecutor(HomeHandler)
            }

            this.getCommand("sethome")!!.apply {
                setExecutor(HomeHandler)
            }

            MotdManager.init(this.dataFolder)
            this.getCommand("motd")!!.apply {
                setExecutor(MotdManager)
            }

            val catWeight = File(dataFolder, "cat")
            if(!catWeight.exists()) {
                catWeight.createNewFile()
                MyLovelyCat.init(this, Math.random() * 10 + 7)
            } else {
                MyLovelyCat.init(this, FileReader(catWeight).readText().toDouble())
            }

            this.getCommand("cat")!!.apply {
                setExecutor(MyLovelyCat)
            }

            this.getCommand("feed")!!.apply {
                setExecutor(MyLovelyCat)
            }

            // Event Register

            server.pluginManager.registerEvents(this, this)
            server.pluginManager.registerEvents(PrivateHandler, this)
            server.pluginManager.registerEvents(ShowManager, this)
            server.pluginManager.registerEvents(PrefixManager, this)
            server.pluginManager.registerEvents(MotdManager, this)

            server.onlinePlayers.forEach {
                it.setDisplayName(
                    "${ChatColor.AQUA}[${parseWorld(it.location.world?.name)}${ChatColor.AQUA}] ${it.name}")
            }
            //launchCheckUpdatesTask()
            logger.warning("${ChatColor.RED}因为lama穷导致CI没钱续费 :( 自动更新无了")
            logger.info("${ChatColor.GREEN}HomeEntity Bukkit注册完毕!")

            val listen = config.getLong("relay.listen")
            val token = config.getString("relay.token")!!
            logger.info("启动转发机器人v2 访问密钥: ${token.substringAfter(':').substring(0..10).padEnd(35, '*')}. 机器人将监听群组: $listen ")
            botInstance = RelayBotV2(listen, token)
            logger.info("${ChatColor.GREEN}转发机器人启动完毕!")
        }.also {
            logger.info("${ChatColor.GREEN}HomeEntity 加载完毕! 用时 $it 毫秒")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisable() {
        WarpHandlerV2.save()
        AuthHandler.save()
        PrefixManager.save()
        MotdManager.save(this.dataFolder)

        val catFile = File(dataFolder, "cat")
        catFile.delete()
        catFile.createNewFile()
        FileWriter(catFile).apply {
            write(MyLovelyCat.weight().toString())
            flush()
            close()
        }

        runBlocking {
            botInstance.shutdown()
        }

        logger.info("${ChatColor.RED}Reached goal 'shutdown'")
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
                            //TODO 迁移到Github Actions
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

    fun parseWorld(name: String?): String {
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
        AuthHandler.setLoginState(e.player.uniqueId, false)
        e.joinMessage = "${ChatColor.GRAY}[${ChatColor.GREEN}+${ChatColor.GRAY}] ${ChatColor.GRAY}${e.player.name}"
        e.player.sendMessage("${ChatColor.GRAY}============================")
        e.player.sendMessage("${ChatColor.GOLD}  欢迎来到${config.getString("main.serverName")}")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'.l <密码>'       来登录")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'.r <密码> <密码>' 来注册")
        e.player.sendMessage("${ChatColor.RED}  若忘记密码请找在线管理员重置")
        e.player.sendMessage("${ChatColor.GRAY}============================")
        AuthHandler.limit(e.player)
        e.player.setDisplayName(
            "${ChatColor.AQUA}[${ChatColor.RESET}未登录${ChatColor.AQUA}] ${e.player.name}")

        Bukkit.getScheduler().runTaskLater(this, Runnable {
            if(AuthHandler.getLoginState(e.player.uniqueId)) {
                AuthHandler.removeLimit(e.player)
                e.player.setDisplayName(
                        "${ChatColor.AQUA}[${parseWorld(e.player.world.name)}${ChatColor.AQUA}] ${e.player.name}")
            } else {
                AuthHandler.setLoginState(e.player.uniqueId, false)
                e.player.kickPlayer("${ChatColor.RED}登录验证超时")
            }
        }, 30 * 20)

        botInstance.say("[+] ${e.player.name}")
    }

    @EventHandler
    fun onPlayerDamage(e: EntityDamageByEntityEvent) {
        if(e.entity is Player && AuthHandler.getLoginState(e.entity.uniqueId)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
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
        // WIP
    }
}
