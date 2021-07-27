package cn.thelama.homeent.motd

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.secure.AuthHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object MotdManager : Listener, CommandExecutor {
    private lateinit var lines: MutableList<String>

    fun init(dataFolder: File) {
        val motd = File(dataFolder, "motd")
        lines = if(!motd.exists()) {
            motd.createNewFile()
            mutableListOf("", "")
        } else {
            val fr = FileReader(motd, charset("UTF-8"))
            fr.readLines().toMutableList()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "motd" && ((sender is Player && AuthHandler.maintainer(sender.uniqueId, sender)) || sender is ConsoleCommandSender)) {
            if(args.size >= 3) {
                when(args[0]) {
                    "set" -> {
                        runCatching {
                            lines[args[1].toInt() - 1] = args.drop(2).joinToString(separator = " ") { it }.replace('&', ChatColor.COLOR_CHAR)
                            sender.sendMessage("${ChatColor.GREEN}设置成功")
                        }.onFailure {
                            if(it is NumberFormatException) {
                                sender.sendMessage("${ChatColor.RED}请输入一个正确的数字!")
                            }

                            if(it is ArrayIndexOutOfBoundsException) {
                                sender.sendMessage("${ChatColor.RED}行号不正确! 1 或 2")
                            }
                        }
                    }

                    "clr" -> {
                        runCatching {
                            lines[args[1].toInt() - 1] = ""
                            sender.sendMessage("${ChatColor.GREEN}设置成功")
                        }.onFailure {
                            if(it is NumberFormatException) {
                                sender.sendMessage("${ChatColor.RED}请输入一个正确的数字!")
                            }

                            if(it is ArrayIndexOutOfBoundsException) {
                                sender.sendMessage("${ChatColor.RED}行号不正确! 1 或 2")
                            }
                        }
                    }
                }
            } else {
                lines.forEach {
                    sender.sendMessage(it)
                }            }
        }
        return true
    }

    @EventHandler
    fun onPing(e: ServerListPingEvent) {
        e.maxPlayers = Bukkit.getOnlinePlayers().size + 1
        e.motd = lines.joinToString(separator = "\n") { it }
    }
    
    fun save(dataFolder: File) {
        val motd = File(dataFolder, "motd")
        if(!motd.exists()) {
            motd.createNewFile()
        }
        val fw = FileWriter(motd, charset("UTF-8"))
        fw.write(lines.joinToString(separator = "\n"))
        fw.flush()
        fw.close()
    }
}