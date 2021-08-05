package cn.thelama.homeent.notice

import org.bukkit.Bukkit
import org.bukkit.entity.Player

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