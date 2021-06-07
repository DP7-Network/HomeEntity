@file:JvmName("HomeEntity")
package cn.thelama.homeent

import cn.thelama.homeent.back.BackHandler
import cn.thelama.homeent.exit.ExitHandler
import cn.thelama.homeent.notice.Notice
import cn.thelama.homeent.p.PrivateHandler
import cn.thelama.homeent.relay.RelayBot
import cn.thelama.homeent.session.SessionHandler
import cn.thelama.homeent.show.ShowCompleter
import cn.thelama.homeent.show.ShowHandler
import cn.thelama.homeent.show.ShowManager
import cn.thelama.homeent.slime.SlimeHandler
import cn.thelama.homeent.tpa.*
import cn.thelama.homeent.warp.LocationWrapper
import cn.thelama.homeent.warp.WarpCompleter
import cn.thelama.homeent.warp.WarpHandler
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
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
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader
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
        const val VERSION = "1.4.2 Devel"
        const val JENKINS_BASE = "https://ci.thelama.cn"
        lateinit var instance: HomeEntity
        lateinit var COMMIT_HASH: String
        lateinit var BRANCH: String
        var BUILD_NUMBER: Int = 0
    }
    private val gson = Gson()
    val unloggedInPlayers = mutableListOf<UUID>()
    lateinit var warps: HashMap<String, LocationWrapper>
    lateinit var botInstance: RelayBot
    lateinit var passwords: HashMap<UUID, String>
    lateinit var maintainers: ArrayList<UUID>
    lateinit var minecraftTranslation: HashMap<String, String>
    lateinit var globalNetworkProxy: Proxy
    lateinit var httpClient: OkHttpClient
    val lastTeleport: HashMap<UUID, Location> = HashMap()

    private lateinit var warpsFile: File
    private lateinit var maintainersFile: File
    private lateinit var passwordsFile: File

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
            logger.info("${ChatColor.GREEN}Welcome to HomeEntity $VERSION ($BRANCH@${COMMIT_HASH.substring(7)})")
            if(!dataFolder.exists()) {
                dataFolder.mkdir()
            }
            logger.info("  Loading warps")
            measureTimeMillis {
                warpsFile = File(dataFolder, "warps.json")
                if (!warpsFile.exists()) {
                    warpsFile.createNewFile()
                }
                logger.info("    Parsing file...")
                gson.fromJson<HashMap<String, LocationWrapper>?>(FileReader(warpsFile), object : TypeToken<HashMap<String, LocationWrapper>>() {}.type).also {
                    warps = it ?: HashMap()
                }
            }.also {
                logger.info("    ${ChatColor.GREEN}Warps loaded in $it ms")
            }

            logger.info("  Loading main configuration")
            measureTimeMillis {
                saveDefaultConfig()
                if(this.config.getBoolean("proxy.enable")) {
                    if(config.getString("proxy.type")!!.toLowerCase() == "socks") {
                        this.globalNetworkProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(config.getString("proxy.ip"), config.getInt("proxy.port")))
                    } else if(config.getString("proxy.type")!!.toLowerCase() == "http") {
                        this.globalNetworkProxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(config.getString("proxy.ip"), config.getInt("proxy.port")))
                    } else {
                        logger.info("    ${ChatColor.RED}无法确认你的代理类型是什么 :(")
                        this.globalNetworkProxy = Proxy.NO_PROXY
                    }
                } else {
                    this.globalNetworkProxy = Proxy.NO_PROXY
                }

                httpClient = OkHttpClient.Builder().proxy(globalNetworkProxy).build()
            }.also {
                logger.info("    ${ChatColor.GREEN}main loaded in $it ms")
            }

            logger.info("  Loading passwords")
            measureTimeMillis {
                passwordsFile = File(dataFolder, "passwords.json")
                if (!passwordsFile.exists()) {
                    passwordsFile.createNewFile()
                }
                logger.info("    Parsing file...")
                gson.fromJson<HashMap<UUID, String>>(FileReader(passwordsFile), object : TypeToken<HashMap<UUID, String>>() {}.type).also {
                    passwords = it ?: HashMap()
                }
            }.also {
                logger.info("    ${ChatColor.GREEN}Passwords loaded in $it ms")
            }

            logger.info("  Loading translations")
            measureTimeMillis {
                logger.info("    Parsing file...")
                gson.fromJson<HashMap<String, String>>(InputStreamReader(this.javaClass.classLoader.getResourceAsStream("zh-cn.lang")!!), object : TypeToken<HashMap<String, String>>() {}.type).also {
                    minecraftTranslation = it ?: HashMap()
                }
            }.also {
                logger.info("    ${ChatColor.GREEN}Translations loaded in $it ms")
            }

            logger.info("  Loading Maintainers")
            measureTimeMillis {
                maintainersFile = File(dataFolder, "maintainers.json")
                if(!maintainersFile.exists()) {
                    maintainersFile.createNewFile()
                }
                logger.info("    Parsing file...")
                gson.fromJson<ArrayList<UUID>>(FileReader(maintainersFile), object: TypeToken<ArrayList<UUID>>() {}.type).also {
                    maintainers = it ?: ArrayList()
                }
            }.also {
                logger.info("    ${ChatColor.GREEN}Maintainers loaded in $it ms")
            }

            logger.info("  Register commands...")

            this.getCommand("warp")!!.apply {
                setExecutor(WarpHandler)
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
                setExecutor(SessionHandler)
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


            logger.info("  Register events...")

            server.pluginManager.registerEvents(this, this)
            server.pluginManager.registerEvents(PrivateHandler, this)
            server.pluginManager.registerEvents(ShowManager, this)

            logger.info("  Finalizing...")

            server.onlinePlayers.forEach {
                it.setDisplayName("${ChatColor.AQUA}[${parseWorld(it.location.world?.name)}${ChatColor.AQUA}] ${it.name}")
            }
            launchCheckUpdatesTask()
            logger.info("${ChatColor.GREEN}Reached goal 'initialize'")
            logger.info("Launching Relay Bot")
            botInstance = RelayBot(config.getLong("relay.listen"), config.getString("relay.token")!!)
            logger.info("${ChatColor.GREEN}Reached goal 'relay'")
        }.also {
            logger.info("${ChatColor.GREEN}HomeEntity Initialized Complete in $it ms")
        }
    }

    override fun onDisable() {
        logger.info("Shutting down HomeEntity...")
        logger.info("  Save data...")
        logger.info("    Saving warps")
        writeFile(warpsFile, gson.toJson(warps))
        logger.info("    Saving passwords")
        writeFile(passwordsFile, gson.toJson(passwords))
        logger.info("    Saving maintainers")
        writeFile(maintainersFile, gson.toJson(maintainers))
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
                sender.sendMessage("${ChatColor.AQUA}HomeEntity ${ChatColor.RESET}- ${ChatColor.GREEN}$VERSION ${ChatColor.RESET}| ${ChatColor.ITALIC}${ChatColor.YELLOW}Build $BUILD_NUMBER $BRANCH@${COMMIT_HASH.substring(7)}")
            }


        }
        return true
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        e.quitMessage = "${ChatColor.GRAY}[${ChatColor.RED}-${ChatColor.GRAY}] ${ChatColor.GRAY}${e.player.name}"
        botInstance.sendMessage("[-] ${e.player.name}")
    }
    
    @EventHandler
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if(e.from.world != e.to?.world) {
            e.player.setDisplayName("${ChatColor.AQUA}[${parseWorld(e.to?.world?.name)}${ChatColor.AQUA}] ${e.player.name}")
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
                "${ChatColor.DARK_RED}地狱"
            }

            "world_the_end" -> {
                "${ChatColor.LIGHT_PURPLE}末地"
            }

            else -> {
                "${ChatColor.DARK_BLUE}$name"
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        synchronized(unloggedInPlayers) {
            unloggedInPlayers.add(e.player.uniqueId)
        }
        e.joinMessage = "${ChatColor.GRAY}[${ChatColor.GREEN}+${ChatColor.GRAY}] ${ChatColor.GRAY}${e.player.name}"
        e.player.sendMessage("${ChatColor.GRAY}============================")
        e.player.sendMessage("${ChatColor.GOLD}      欢迎来到${config.getString("main.serverName")}      ")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'/l <密码>'       来登录  ")
        e.player.sendMessage("${ChatColor.AQUA}  请发送'/r <密码> <密码>' 来注册  ")
        e.player.sendMessage("${ChatColor.RED}   <如果忘记密码请找管理员重置>  ")
        e.player.sendMessage("${ChatColor.GRAY}============================")
        e.player.sendMessage("${ChatColor.GRAY}P.S. '.'和'/'做前缀都可以")
        SessionHandler.limit(e.player)

        Bukkit.getScheduler().runTaskLater(this, Runnable {
            synchronized(unloggedInPlayers) {
                if(unloggedInPlayers.contains(e.player.uniqueId)) {
                    unloggedInPlayers.remove(e.player.uniqueId)
                    e.player.kickPlayer("${ChatColor.RED} 给你的登陆时间没有那么多，按快点OK?")
                } else {
                    SessionHandler.removeLimit(e.player)
                }
            }
        }, 30 * 20)

        botInstance.sendMessage("[+] ${e.player.name}")
    }

    @EventHandler
    fun onPlayerDamage(e: EntityDamageByEntityEvent) {
        if(e.entity is Player && unloggedInPlayers.contains(e.entity.uniqueId)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        when(e.player.name) {
            "Lama3L9R" -> {
                e.format = "${ChatColor.AQUA}[${ChatColor.RESET}${parseWorld(e.player.location.world?.name)}${ChatColor.AQUA}] ${ChatColor.BLUE}Dev ${ChatColor.YELLOW}${e.player.name}${ChatColor.RESET}: ${ChatColor.RESET}%2\$s"
            }

            else -> {
                e.format = "${ChatColor.AQUA}[${ChatColor.RESET}${parseWorld(e.player.location.world?.name)}${ChatColor.AQUA}] ${ChatColor.YELLOW}${e.player.name}${ChatColor.RESET}: ${ChatColor.RESET}%2\$s"
            }
        }

        Notice.parseMessage(e.message).forEach {
            it.sendTitle("${ChatColor.YELLOW}有人提到你", "${ChatColor.YELLOW}${e.player.name}${ChatColor.WHITE} 在聊天消息中提到了你，快去看看", 10, 3 * 20, 10)
        }

        if(!e.isCancelled) {
            botInstance.sendMessage("${e.player.name}: ${e.message}")
        }
    }

    fun isLogin(p: Player): Boolean {
        return p.uniqueId !in unloggedInPlayers
    }

    fun sha256(str: String): String {
        return String(Hex.encodeHex(MessageDigest.getInstance("SHA-256").digest(str.toByteArray(charset("UTF-8"))), false))
    }

    fun checkCredentials(p: Player, str: String): Boolean {
        if(passwords.containsKey(p.uniqueId)) {
            return passwords[p.uniqueId] == sha256(str)
        }
        return false
    }

    fun register(p: Player, str: String): Boolean {
        return if(!passwords.containsKey(p.uniqueId)) {
            passwords[p.uniqueId] = sha256(str)
            true
        } else {
            false
        }
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
                Bukkit.broadcastMessage("${ChatColor.GREEN}HomeEntity: 可用更新已找到准备更新!")
                FileUtils.copyToFile(URL("http://s1.lama3l9r.net/job/$stream/lastSuccessfulBuild/artifact/build/libs/HomeEntity-1.0-SNAPSHOT-all.jar").openConnection(
                    Proxy(Proxy.Type.SOCKS, InetSocketAddress("192.168.1.102", 1089))).getInputStream(), File(Bukkit.getUpdateFolderFile(), this.file.name))
                Bukkit.broadcastMessage("更新已下载完毕！准备重载")
                YumAPI.reload(this)
            }
        }
    }
}