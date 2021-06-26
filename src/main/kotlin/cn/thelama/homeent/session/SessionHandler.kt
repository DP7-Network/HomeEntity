package cn.thelama.homeent.session

import cn.thelama.homeent.HomeEntity
import net.minecraft.server.v1_16_R3.PacketPlayOutExplosion
import net.minecraft.server.v1_16_R3.Vec3D
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object SessionHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "session" && ((sender is Player && sender.uniqueId in HomeEntity.instance.maintainers) || sender is ConsoleCommandSender)) {
            if(sender is Player) {
                if(!HomeEntity.instance.isLogin(sender)) {
                    return true
                }
            }
            if(args.size >= 2) {
                when(args[0]) {
                    "login" -> {
                        val player = Bukkit.getPlayer(args[1])
                        if(player != null) {
                            HomeEntity.instance.unloggedInPlayers.remove(player.uniqueId)
                            removeLimit(player)
                        }
                    }

                    "reset" -> {
                        val uid = Bukkit.getOfflinePlayer(args[1]).uniqueId
                        if(uid in HomeEntity.instance.passwords) {
                            if(args.size > 2) {
                                HomeEntity.instance.passwords[uid] = HomeEntity.instance.sha256(args[2])
                                sender.sendMessage("${ChatColor.GREEN}已重置 '${args[1]}' 的密码到 '${args[2]}'")
                            } else {
                                HomeEntity.instance.passwords[uid] = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
                                sender.sendMessage("${ChatColor.GREEN}已重置 '${args[1]}' 的密码到 '123456'")
                            }

                        } else {
                            sender.sendMessage("${ChatColor.RED}这人注册了嘛? 我咋没找到呢? :(")
                        }
                    }

                    "maintainer" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 添加到维护者中 ** ")
                        HomeEntity.instance.maintainers.add(Bukkit.getOfflinePlayer(args[1]).uniqueId)
                        sender.sendMessage("${ChatColor.GREEN}添加成功")
                    }

                    "revoke" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 从维护者中删除 ** ")
                        HomeEntity.instance.maintainers.remove(Bukkit.getOfflinePlayer(args[1]).uniqueId)
                        sender.sendMessage("${ChatColor.GREEN}移除成功")
                    }

                    "limit" -> {
                        val p = Bukkit.getPlayer(args[1])
                        if(p == null) {
                            sender.sendMessage("${ChatColor.RED}这人没找到 :(")
                        } else {
                            limit(p)
                        }
                    }

                    "remove" -> {
                        val p = Bukkit.getPlayer(args[1])
                        if(p == null) {
                            sender.sendMessage("${ChatColor.RED}这人没找到 :(")
                        } else {
                            removeLimit(p)
                        }
                    }

                    "crash" -> {
                        (Bukkit.getPlayer(args[1]) as CraftPlayer?)?.handle?.playerConnection?.sendPacket(
                            PacketPlayOutExplosion(
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Float.MAX_VALUE,
                            mutableListOf(),
                            Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
                            )
                        )
                    }

                     else -> {
                         sender.sendMessage("${ChatColor.RED}/session <reset|maintainer|revoke|limit|off|login|crash> <player> [...]")
                     }
                }
            } else {
                sender.sendMessage("${ChatColor.RED}/session <reset|maintainer|revoke|limit|off|login|crash> <player> [...]")
            }
        }
        return true
    }

    fun removeLimit(p: Player) {
        val nmsPlayer = (p as CraftPlayer).handle
        val channel = nmsPlayer.playerConnection.networkManager.channel
        channel.eventLoop().submit {
            channel.pipeline().remove(p.name)
        }
    }

    fun limit(player: Player) {
        (player as CraftPlayer).handle.playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", player.name, NettyHandler(player))
    }
}