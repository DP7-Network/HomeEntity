package cn.thelama.homeent.tpa

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object TPACompleter : TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, lable: String, args: Array<out String>): MutableList<String> {
        if(sender is Player && args.size == 1) {
            return Bukkit.getOnlinePlayers().filter { it.name.startsWith(args[0]) }.map { it.name }.toMutableList()
        } else if (args.isEmpty()) {
            return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        }
        return mutableListOf()
    }
}