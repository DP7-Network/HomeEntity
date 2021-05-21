package cn.thelama.homeent.show

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object ShowCompleter : TabCompleter {
    private val possible: List<String> = listOf("head", "body", "leg", "foot", "left", "main", "inv")
    override fun onTabComplete(sender: CommandSender, command: Command, lable: String, args: Array<out String>): List<String>? {
        if(command.name == "show") {
            if(args.isNotEmpty()) {
                return possible.filter { it.startsWith(args[0]) }
            }
        }
        return null
    }
}