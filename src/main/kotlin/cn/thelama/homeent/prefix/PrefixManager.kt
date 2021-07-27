package cn.thelama.homeent.prefix

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.module.ModuleCommand
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.secure.AuthHandler
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*

object PrefixManager : CommandExecutor, ModuleCommand, Listener {
    private val prefixes: MutableMap<UUID, PrefixWrapper> = ModuledPlayerDataManager.getAllTyped<PrefixWrapper>("prefix", object: TypeToken<PrefixWrapper>() {}.type).apply {
        this[Bukkit.getOfflinePlayer("Lapis_Apple").uniqueId] = PrefixWrapper("${ChatColor.GOLD}LSP")
    }

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if((sender is Player && AuthHandler.maintainer(sender.uniqueId, sender)) || sender is ConsoleCommandSender) {
            if(args.isNotEmpty()) {
                when(args.size) {
                    2 -> {
                        if(args[1] == "\$null") {
                            if(args[0] != "Lapis_Apple") {
                                prefixes -= Bukkit.getOfflinePlayer(args[0]).uniqueId
                            }
                            sender.sendMessage("${ChatColor.GREEN}删除称号成功!")
                        } else {
                            if(args[0] != "Lapis_Apple") {
                                prefixes[Bukkit.getOfflinePlayer(args[0]).uniqueId] = PrefixWrapper(args[1].replace('&', ChatColor.COLOR_CHAR))
                            }
                            sender.sendMessage("${ChatColor.GREEN}更新称号成功!")
                        }
                    }

                    1 -> {
                        if(sender !is Player) {
                            sender.sendMessage("${ChatColor.RED}请指定一名玩家")
                            return true
                        }

                        if(args[0] == "\$null") {
                            if(sender.name != "Lapis_Apple") {
                                prefixes -= sender.uniqueId
                            }

                            sender.sendMessage("${ChatColor.GREEN}删除称号成功!")
                        } else {
                            if(sender.name != "Lapis_Apple") {
                                prefixes[sender.uniqueId] = PrefixWrapper(args[0].replace('&', ChatColor.COLOR_CHAR))
                            }
                            sender.sendMessage("${ChatColor.GREEN}更新称号成功!")
                        }
                    }

                    else -> {
                        sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                    }
                }
            } else {
                sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }
        } else {
            sender.sendMessage("${ChatColor.RED}Permission denied(maintainer).")
        }
        return true
    }

    override fun save() {
        ModuledPlayerDataManager.setAllTyped("prefix", prefixes)
    }

    @EventHandler
    fun onPlayerChat(e: AsyncPlayerChatEvent) {
        if(e.player.uniqueId !in prefixes) {
            e.format =
                "${ChatColor.AQUA}[${ChatColor.RESET}${HomeEntity.instance.parseWorld(e.player.location.world?.name)}${ChatColor.AQUA}] " +
                        "${ChatColor.YELLOW}${e.player.name}${ChatColor.RESET}: " +
                        "${ChatColor.RESET}%2\$s"
        } else {
            e.format =
                "${ChatColor.AQUA}[${ChatColor.RESET}${HomeEntity.instance.parseWorld(e.player.location.world?.name)}${ChatColor.AQUA}] " +
                        "${ChatColor.RESET}${prefixes[e.player.uniqueId]?.prefix ?: ""}${ChatColor.RESET} " +
                        "${ChatColor.YELLOW}${e.player.name}${ChatColor.RESET}: " +
                        "${ChatColor.RESET}%2\$s"
        }
    }

    data class PrefixWrapper(@field:JvmField val prefix: String)
}