package cn.thelama.homeent.back

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

object BackHandler : CommandExecutor, Listener {
    private val lastTeleport = mutableMapOf<UUID, Location>()

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "back") {
            if(sender is Player) {
                if(sender.uniqueId in lastTeleport) {
                    sender.teleport(lastTeleport[sender.uniqueId]!!)
                } else {
                    sender.sendMessage("${ChatColor.RED}无法确定你从哪里来!")
                }
            }
        }
        return true
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        lastTeleport[e.entity.uniqueId] = e.entity.location
    }

    @EventHandler
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        lastTeleport[e.player.uniqueId] = e.from
    }
}