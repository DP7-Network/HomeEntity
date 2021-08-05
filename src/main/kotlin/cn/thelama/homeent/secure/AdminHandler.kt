package cn.thelama.homeent.secure

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.autoupdate.UpdateManager
import net.minecraft.network.protocol.game.PacketPlayOutExplosion
import net.minecraft.world.phys.Vec3D
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.*
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player

object AdminHandler : CommandExecutor, TabCompleter {
    private val operations = listOf("maintainer", "revoke", "crash", "restart", "shutdown", "sync", "stop-update-task", "launch-update-task", "status-update-task")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(command.name == "admin" && ((sender is Player && AuthHandler.maintainer(sender.uniqueId, sender)) || sender is ConsoleCommandSender)) {
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

                    "restart" -> {
                        val time = args[1].toIntOrNull()
                        if(time == null || time <= 0) {
                            sender.sendMessage("请输入一个非0的数字!")
                            return true
                        }

                        val reason = if(args.size >= 3) {
                            args[2]
                        } else {
                            "无"
                        }

                        UpdateManager.scheduleShutdown(reason, sender.name, time = time, shutdown = false)
                    }

                    "shutdown" -> {
                        val time = args[1].toIntOrNull()
                        if(time == null || time <= 0) {
                            sender.sendMessage("请输入一个非0的数字!")
                            return true
                        }

                        val reason = if(args.size >= 3) {
                            args[2]
                        } else {
                            "无"
                        }

                        UpdateManager.scheduleShutdown(reason, sender.name, time = time, shutdown = true)
                    }

                    else -> {
                        sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                    }
                }
            } else {
                if(args.isNotEmpty()) {
                    when(args[0]) {
                        "sync" -> {
                            val stream = if(args.size >= 2) {
                                args[1]
                            } else {
                                HomeEntity.REPO
                            }

                            if("async" in args) {
                                Bukkit.getScheduler().runTaskAsynchronously(HomeEntity.instance, Runnable {
                                    UpdateManager.updateSync(stream = stream)
                                })
                            } else {
                                UpdateManager.updateSync(stream = stream)
                            }
                        }

                        "stop-update-task" -> {
                            if(!UpdateManager.checkerStatus()) {
                                sender.sendMessage("${ChatColor.RED}自动更新任务已停止!")
                            } else {
                                UpdateManager.stopAsyncUpdateChecker()
                                sender.sendMessage("${ChatColor.RED}自动更新任务已停止!")
                            }
                        }

                        "launch-update-task" -> {
                            if(UpdateManager.checkerStatus()) {
                                sender.sendMessage("${ChatColor.GREEN}自动更新任务已开启!")
                            } else {
                                UpdateManager.launchAsyncUpdateChecker()
                                sender.sendMessage("${ChatColor.GREEN}自动更新任务已开启!")
                            }
                        }

                        "status-update-task" -> {
                            if(UpdateManager.checkerStatus()) {
                                sender.sendMessage("自动更新任务状态: ${ChatColor.GREEN}开启")
                            } else {
                                sender.sendMessage("自动更新任务状态: ${ChatColor.RED}关闭")
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
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return if(args.size == 1) {
            operations.filter { it.startsWith(args[0]) }.toMutableList()
        } else if (args.isEmpty()) {
            operations.toMutableList()
        } else {
            mutableListOf()
        }
    }
}