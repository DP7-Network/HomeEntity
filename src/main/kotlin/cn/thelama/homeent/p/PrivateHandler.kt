package cn.thelama.homeent.p

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.server.TabCompleteEvent

object PrivateHandler : Listener {
    @EventHandler
    fun onComplete(e: TabCompleteEvent) {
        if(e.buffer.startsWith("!p")) {
            val sp = e.buffer.split(" ")
            if(sp.size < 2) {
                e.completions = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
            } else if(sp.size == 2) {
                e.completions = Bukkit.getOnlinePlayers().filter { it.name.startsWith(sp[1]) }.map { it.name }.toMutableList()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncPlayerChatEvent) {
        if(e.message.startsWith("!p")) {
            e.isCancelled = true
            val sp = e.message.split(" ", limit = 3)
            if(sp.size > 2) {
                val p = Bukkit.getPlayer(sp[1]) ?: return
                p.sendMessage("${ChatColor.GRAY}${ChatColor.ITALIC}${e.player.name} 悄悄地对你说: ${sp[2]}")
                e.player.sendMessage("${ChatColor.GRAY}${ChatColor.ITALIC}你悄悄地对 ${sp[1]} 说: ${sp[2]}")
            }
        }
    }
}