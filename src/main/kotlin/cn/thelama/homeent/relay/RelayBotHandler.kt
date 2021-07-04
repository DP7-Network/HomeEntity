package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import kotlinx.coroutines.isActive
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.UUID

object RelayBotHandler : CommandExecutor {
    private val disabledPlayers: MutableList<UUID> = mutableListOf()
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if (command.name == "relay") {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "restart" -> {
                        if (!(sender is Player && sender.uniqueId in HomeEntity.instance.maintainers) || sender !is ConsoleCommandSender) {
                            return true
                        }

                        HomeEntity.instance.botInstance.restartBot()
                    }

                    "on" -> {
                        if (sender is Player) {
                            if (!disabledPlayers.remove(sender.uniqueId)) {
                                sender.sendMessage("${ChatColor.RED}你的Relay功能本来就是开着的!")
                            } else {
                                sender.sendMessage("${ChatColor.GREEN}已启用Relay功能，现在你的消息会被转发到Telegram")
                            }
                        }
                    }

                    "off" -> {
                        if (sender is Player) {
                            if (!disabledPlayers.add(sender.uniqueId)) {
                                sender.sendMessage("${ChatColor.GREEN}你的Relay功能已禁用，现在你的消息不会被转发到Telegram")
                            } else {
                                sender.sendMessage("${ChatColor.RED}你的Relay功能本来就是关着的!")
                            }
                        }
                    }

                    "status" -> {
                        if (sender is Player) {
                            sender.sendMessage(
                                "你的Relay功能 ${
                                    if (sender.uniqueId in disabledPlayers) {
                                        "${ChatColor.RED}已禁用，你的消息不会被转发"
                                    } else {
                                        "${ChatColor.GREEN}已启用，你的消息会被转发"
                                    }
                                }"
                            )
                        }
                    }

                    "toggle" -> {
                        if (sender is Player) {
                            if (sender.uniqueId in disabledPlayers) {
                                disabledPlayers.remove(sender.uniqueId)
                                sender.sendMessage("${ChatColor.RED}你的Relay功能已禁用，现在你的消息不会被转发到Telegram")
                            } else {
                                sender.sendMessage("${ChatColor.GREEN}你的Relay功能已启用，现在你的消息会被转发到Telegram")
                                disabledPlayers.add(sender.uniqueId)
                            }
                        }
                    }

                    else -> {
                        sender.sendMessage("${ChatColor.RED} 无此子指令")
                    }
                }
            }
        } else {
            if(HomeEntity.instance.botInstance.version == 2) {
                sender.sendMessage("服务器的Relay功能 ${if((HomeEntity.instance.botInstance as RelayBotV2).scope.isActive) { "${ChatColor.RED}已停止，大家的消息不会被转发" } else { "${ChatColor.GREEN}已开启，大家的消息会被转发"} }")
            } else {
                sender.sendMessage("服务器的Relay功能 ${ChatColor.YELLOW} Unknown")
            }
        }
        return true
    }

    fun isDisabled(uuid: UUID): Boolean = uuid in disabledPlayers

}
