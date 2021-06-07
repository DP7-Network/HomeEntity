package cn.thelama.homeent.notice

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

/**
 * @author Lama3L9R
 */
object Notice {
    fun parseMessage(message: String): List<Player> {
        var index = message.indexOf('@', 0)
        val players = mutableListOf<Player>()
        while(index != -1) {
            var spaceIndex = message.indexOf(' ', index)
            if(spaceIndex == -1) {
                spaceIndex = message.length - 1
            }
            val player = message.substring(index + 1, spaceIndex)
            Bukkit.getPlayer(player)?.let {
                players.add(it)
            }
            index = message.indexOf('@', spaceIndex)
        }
        return players
    }
}