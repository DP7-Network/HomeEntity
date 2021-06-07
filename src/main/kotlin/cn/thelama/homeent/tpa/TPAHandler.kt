package cn.thelama.homeent.tpa

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TPAHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "tpa" && sender is Player && args.isNotEmpty()) {
            val p = Bukkit.getPlayer(args[0])
            if(p == null) {
                sender.sendMessage("${ChatColor.RED}玩家不在线!")
                return true
            }
            TPManager.newRequestGo(sender, p)
        }
        return true
    }
}