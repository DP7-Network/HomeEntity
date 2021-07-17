package cn.thelama.homeent.secure

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.module.ModuleCommand
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.module.PlayerDataProvider
import com.google.gson.reflect.TypeToken
import net.minecraft.network.protocol.game.PacketPlayOutExplosion
import net.minecraft.world.phys.Vec3D
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

object SecureHandler : CommandExecutor, ModuleCommand, PlayerDataProvider<PlayerSecureEntry?> {
    private val config: MutableMap<UUID, PlayerSecureEntry> = ModuledPlayerDataManager.getAllTyped("secure", object: TypeToken<PlayerSecureEntry>() {}.type)
    private val unloggedInPlayers = mutableListOf<UUID>()

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "secure" && ((sender is Player && maintainer(sender.uniqueId)) || sender is ConsoleCommandSender)) {
            if(sender is Player) {
                if(sender.uniqueId in unloggedInPlayers) {
                    return true
                }
            }
            if(args.size >= 2) {
                when(args[0]) {
                    "login" -> {
                        val player = Bukkit.getPlayer(args[1])
                        if(player != null) {
                            unloggedInPlayers.remove(player.uniqueId)
                            removeLimit(player)
                        }
                    }

                    "reset" -> {
                        val uid = Bukkit.getOfflinePlayer(args[1]).uniqueId
                        val entry = config(uid)
                        if(entry != null) {
                            if(args.size > 2) {
                                entry.encryptedPassword = HomeEntity.instance.sha256(args[2])
                                sender.sendMessage("${ChatColor.GREEN}已重置 '${args[1]}' 的密码到 '${args[2]}'")
                            } else {
                                entry.encryptedPassword = "8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92"
                                sender.sendMessage("${ChatColor.GREEN}已重置 '${args[1]}' 的密码到 '123456'")
                            }

                        } else {
                            sender.sendMessage("${ChatColor.RED}这人注册了嘛? 我咋没找到呢? :(")
                        }
                    }

                    "maintainer" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 添加到维护者中 ** ")
                        config(Bukkit.getOfflinePlayer(args[1]).uniqueId)?.permissionLevel = 1
                        sender.sendMessage("${ChatColor.GREEN}添加成功")
                    }

                    "revoke" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 从维护者中删除 ** ")
                        config(Bukkit.getOfflinePlayer(args[1]).uniqueId)?.permissionLevel = 0
                        sender.sendMessage("${ChatColor.GREEN}移除成功")
                    }

                    "limit" -> {
                        val p = Bukkit.getPlayer(args[1])
                        if(p == null) {
                            sender.sendMessage("${ChatColor.RED}${args[1]} 不在线或不存在")
                        } else {
                            limit(p)
                        }
                    }

                    "remove" -> {
                        val p = Bukkit.getPlayer(args[1])
                        if(p == null) {
                            sender.sendMessage("${ChatColor.RED}${args[1]} 不在线或不存在")
                        } else {
                            removeLimit(p)
                        }
                    }

                    "crash" -> {
                        (Bukkit.getPlayer(args[1]) as CraftPlayer?)?.handle?.b?.sendPacket(
                            PacketPlayOutExplosion(
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Float.MAX_VALUE,
                            mutableListOf(),
                            Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
                            )
                        )
                        Bukkit.broadcastMessage("${ChatColor.RED}${args[1]} R.I.P.")
                    }

                     else -> {
                         sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                     }
                }
            } else {
                sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }
        }
        return true
    }

    fun removeLimit(p: Player) {
        println(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass.simpleName)
        val nmsPlayer = (p as CraftPlayer).handle
        val channel = nmsPlayer.b.a.k
        channel.eventLoop().submit {
            channel.pipeline().remove(p.name)
        }
    }

    fun limit(player: Player) {
        (player as CraftPlayer).handle.b.a.k.pipeline().addBefore("packet_handler", player.name, PlayerLoginNetworkIntercepter(player))
    }

    override fun save() {
        ModuledPlayerDataManager.setAllTyped("secure", config)
    }

    override fun config(uuid: UUID): PlayerSecureEntry? = config[uuid]

    fun maintainer(uuid: UUID): Boolean = config(uuid)?.permissionLevel ?: 0 > 0

    fun checkCredentials(uuid: UUID, encPassword: String): Boolean = config(uuid)?.encryptedPassword == HomeEntity.instance.sha256(encPassword)

    fun register(uuid: UUID, pwd: String): Boolean {
        if(config.containsKey(uuid)) {
            return false
        }

        config[uuid] = PlayerSecureEntry(HomeEntity.instance.sha256(pwd))
        return true
    }

    fun setLoginState(uuid: UUID, state: Boolean) {
        synchronized(unloggedInPlayers) {
            if(state) {
                unloggedInPlayers -= uuid
            } else {
                unloggedInPlayers += uuid
            }
        }
    }

    fun getLoginState(uuid: UUID): Boolean = uuid !in unloggedInPlayers
}