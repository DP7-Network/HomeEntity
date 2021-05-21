package cn.thelama.homeent.report

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object ReportCompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, lable: String, args: Array<out String>): MutableList<String>? {
        return null
    }
}