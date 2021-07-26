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

object AuthHandler : CommandExecutor, ModuleCommand, PlayerDataProvider<PlayerPermissionEntry?> {
    private val config: MutableMap<UUID, PlayerPermissionEntry> = ModuledPlayerDataManager.getAllTyped("secure", object: TypeToken<PlayerPermissionEntry>() {}.type)
    private val unloggedInPlayers = mutableListOf<UUID>()

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "auth" && ((sender is Player && maintainer(sender.uniqueId, sender)) || sender is ConsoleCommandSender)) {
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

    override fun config(uuid: UUID): PlayerPermissionEntry? = config[uuid]

    fun maintainer(uuid: UUID, sender: CommandSender? = null): Boolean {
        val result = config(uuid)?.permissionLevel ?: 0 > 0
        if (!result) sender?.sendMessage("${ChatColor.RED}Permission denied(maintainer).")
        return result
    }

    fun checkCredentials(uuid: UUID, encPassword: String): Boolean = config(uuid)?.encryptedPassword == HomeEntity.instance.sha256(encPassword)

    fun register(uuid: UUID, pwd: String): Boolean {
        if(config.containsKey(uuid)) {
            return false
        }

        config[uuid] = PlayerPermissionEntry(HomeEntity.instance.sha256(pwd))
        return true
    }

    fun setLoginState(uuid: UUID, state: Boolean) {
        if(state) {
            HomeEntity.instance.logger.info("已在未登录玩家移除 $uuid")
            unloggedInPlayers -= uuid
        } else {
            HomeEntity.instance.logger.info("已在未登录玩家添加 $uuid")
            unloggedInPlayers += uuid
        }
    }

    fun getLoginState(uuid: UUID): Boolean = uuid !in unloggedInPlayers
}