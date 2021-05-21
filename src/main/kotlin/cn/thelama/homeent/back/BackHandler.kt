package cn.thelama.homeent.back

import cn.thelama.homeent.HomeEntity
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object BackHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "back") {
            if(sender is Player) {
                if(!HomeEntity.instance.isLogin(sender)) {
                    return true
                }
                if(HomeEntity.instance.lastTeleport.containsKey(sender.uniqueId)) {
                    sender.teleport(HomeEntity.instance.lastTeleport[sender.uniqueId]!!)
                } else {
                    sender.sendMessage("${ChatColor.RED}:( 我不知道你是从哪里来的")
                }
            } else {
                sender.sendMessage("Only players can use this command")
            }
        }
        return true
    }
}