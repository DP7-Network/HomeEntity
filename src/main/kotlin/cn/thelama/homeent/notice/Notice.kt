package cn.thelama.homeent.notice

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author MaxelBlack
 */
object Notice : Listener {
    private var maxLength = 0;
    private val playerList = LinkedList<Player>();

    fun parseMessage(message: String, players: List<Player> = playerList): List<Player> {
        return listOf()
        val foundPlayers = ArrayList<Player>(1)
        var startIndex = 0
        var b = true
        while (b) {
            val index = message.indexOf('@', startIndex)
            if (index == -1) break
            val sub: String
            if (message.length > maxLength) {
                val lastIndex1 = index + maxLength
                startIndex = lastIndex1
                sub = message.substring(index, lastIndex1)
            } else {
                b = false
                sub = message
            }
            var b1 = false
            var player: Player? = null
            for (p in playerList) {
                val tmpName = p.name
                if (sub.contains(tmpName)) {
                    b1 = true
                    player = p
                }
            }
            if (b1) foundPlayers += player!!
        }
        return foundPlayers
    }

    @EventHandler
    fun playerUpdate0(e: PlayerJoinEvent) {
        playerUpdateAdd(e.player)
    }

    @EventHandler
    fun playerUpdate0(e: PlayerQuitEvent) {
        playerUpdateRemove(e.player)
    }

    @EventHandler
    fun playerUpdate0(e: PlayerKickEvent) {
        playerUpdateRemove(e.player)
    }

    fun playerUpdateAdd(player: Player) {
        return
        val length = player.name.length
        var b = true
        playerList.forEachIndexed { index, s ->
            if (s.name.length < length) return
            else {
                playerList.add(index, player)
                b = false
                return@forEachIndexed
            }
        }
        if (b) playerList += player
        maxLength = playerList.last.name.length
    }

    private fun playerUpdateRemove(player: Player) {
        return
        playerList -= player
        maxLength = playerList.last.name.length
    }

}