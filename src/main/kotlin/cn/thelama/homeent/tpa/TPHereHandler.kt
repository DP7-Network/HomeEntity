package cn.thelama.homeent.tpa

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

object TPHereHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "tphere" && sender is Player && args.isNotEmpty()) {
            val p = Bukkit.getPlayer(args[0])
            if(p == null) {
                sender.sendMessage("${ChatColor.RED}玩家不在线!")
                return true
            }

            val cost = ceil(sqrt((p.location.x - sender.location.x).pow(2.0) + (p.location.z - sender.location.z).pow(2.0)) / 100)
            if(sender.health < cost) {
                sender.sendMessage("${ChatColor.RED}无法传送! 您当前的血量不足使 ${args[0]} 传送您的位置! 您的血量: ${floor(sender.health)}❤ 需要血量: $cost❤")
                return true
            } else {
                if(!(args.size > 1 && args[1] == "confirm")) {
                    val msg = ComponentBuilder("${ChatColor.GOLD}将 ${args[0]} 传送到您的位置需要消耗血量: ${ChatColor.RESET}$cost${ChatColor.RED}❤${ChatColor.GOLD} 您当前血量: ${ChatColor.RESET}${floor(sender.health)}${ChatColor.RED}❤ ")
                    val btn = ComponentBuilder("${ChatColor.GRAY}[${ChatColor.GREEN}接受${ChatColor.GRAY}]")
                    btn.event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/$lable ${args.joinToString(separator = " ")} confirm confirm"))
                    btn.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("发送一个传送到这里请求")))
                    msg.append(btn.create())
                    sender.spigot().sendMessage(*msg.create())
                    return true
                }
            }

            sender.sendMessage("${ChatColor.GREEN}请求已发送成功!")
            TPManager.newRequestHere(sender, p)
        }
        return true
    }
}