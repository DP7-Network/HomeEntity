package cn.thelama.homeent.report

import cn.thelama.homeent.HomeEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

object ReportHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "report") {
            if(sender is Player) {
                if(!HomeEntity.instance.isLogin(sender)) {
                    return true
                }
            }
            if(args.isNotEmpty()) {
                when(args[0]) {
                    "bug" -> {
                        if(args.size < 2) {
                            sender.sendMessage("/report bug 具体bug内容")
                        }
                        if(sender !is Player) {
                            sender.sendMessage("LAMA IS LAZY TO HANDLE CONSOLE REPORT SUBMIT")
                            return true
                        }
                        HomeEntity.instance.reports.add(0, ReportEntry(HomeEntity.REPORT_TYPE_BUG, args[1], sender.uniqueId, true, null, null, HomeEntity.REPORT_REPLY_PENDING))
                    }

                    "feature" -> {
                        if(args.size < 2) {
                            sender.sendMessage("/report bug 具体bug内容")
                        }
                        if(sender !is Player) {
                            sender.sendMessage("LAMA IS LAZY TO HANDLE CONSOLE REPORT SUBMIT")
                            return true
                        }
                        HomeEntity.instance.reports.add(0, ReportEntry(HomeEntity.REPORT_TYPE_FEATURE, args[1], sender.uniqueId, true, null, null, HomeEntity.REPORT_REPLY_PENDING))
                    }

                    "reopen" -> {
                        TODO("Set Report State")
                    }

                    "close" -> {
                        if(args.size < 2) {
                            sender.sendMessage("")
                        }
                    }

                    "list" -> {
                        sender.sendMessage("--- Opened reports ---")
                        HomeEntity.instance.reports.forEachIndexed { i, e ->
                            if(!e.isOpen) {
                                return true
                            }
                            sender.sendMessage(" #$i Status: ${ChatColor.GREEN}OPEN By: ${Bukkit.getOfflinePlayer(e.reportPlayer).name} Content: ${e.msg}")
                        }
                    }

                    "handle" -> { // report handle id ...
                        TODO("Manage and handle Report")
                    }

                    "maintainers" -> {
                        if(sender is ConsoleCommandSender) {
                            when(args[1]) {
                                "add" -> {
                                    if(args.size < 2) {
                                        return true
                                    }
                                    HomeEntity.instance.maintainers.add(Bukkit.getOfflinePlayer(args[1]).uniqueId)
                                    sender.sendMessage("Added ${args[1]} to maintainers")
                                }

                                "remove" -> {

                                }

                                "list" -> {
                                    sender.sendMessage("HomeEntity Maintainers: ")
                                    HomeEntity.instance.maintainers.forEach {
                                        sender.sendMessage(" ${Bukkit.getOfflinePlayer(it)} -> $it")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                sender.sendMessage("/report <bug|feature|reopen|close|handle> <args...>")
            }
        }
        return true
    }
}