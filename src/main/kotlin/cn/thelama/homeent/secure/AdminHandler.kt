package cn.thelama.homeent.secure

import cn.thelama.homeent.HomeEntity
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

object AdminHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(command.name == "admin" && ((sender is Player && AuthHandler.maintainer(sender.uniqueId)) || sender is ConsoleCommandSender)) {
            if(args.size >= 2) {
                when(args[0]) {
                    "maintainer" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 添加到维护者中 ** ")
                        AuthHandler.config(Bukkit.getOfflinePlayer(args[1]).uniqueId)?.permissionLevel = 1
                        sender.sendMessage("${ChatColor.GREEN}添加成功")
                    }

                    "revoke" -> {
                        Bukkit.getServer().logger.warning("${ChatColor.RED}${ChatColor.STRIKETHROUGH} ** '${args[1]}' 已被 '${sender.name}' 从维护者中删除 ** ")
                        AuthHandler.config(Bukkit.getOfflinePlayer(args[1]).uniqueId)?.permissionLevel = 0
                        sender.sendMessage("${ChatColor.GREEN}移除成功")
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
                        Bukkit.broadcastMessage("${ChatColor.RED}${sender.name}对${args[1]}使用了神罚! ${ChatColor.BOLD}${ChatColor.UNDERLINE}奠")
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
}