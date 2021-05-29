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
 * @author Maxel Black
 */
object Notice : Listener {
    private var maxLength = 0
    private val playerList = Collections.synchronizedList(LinkedList<Player>())
    private val logger = Bukkit.getLogger()

    fun parseMessage(message: String, players: List<Player> = playerList): List<Player> {
        return listOf()
//        logger.info("[Notice:MessageParse] Looking for ${players.size} name(s) in message...")
        val foundPlayers = ArrayList<Player>(1)
        val playerSize = playerList.size
        var startIndex = 0
        while (true) {
            val index = message.indexOf('@', startIndex)
            if (index == -1) break
            val sub = if (message.length - index > maxLength) {
                val startIndex1 = index + 1
                val lastIndex1 = startIndex1 + maxLength
                message.substring(startIndex1, lastIndex1)
            } else message.substring(index + 1)
            logger.info("[Notice:MessageParse] Found '@' at index $index, looking for $playerSize name(s) in '$sub'...")
            var b1 = false
            var player: Player? = null
            for (p in players) {
                val tmpName = p.name
                val tmpIndex = sub.indexOf(tmpName)
                if (tmpIndex == 0) {
                    b1 = true
                    player = p
                    startIndex = index + 1 + tmpName.length
                }
            }
            if (b1) {
                logger.info("[Notice:MessageParse] Found a player name: ${player!!.name}")
                foundPlayers += player
            }
            else if (index == message.lastIndex) break
            else startIndex = index + 1
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
        val name = player.name
        val length = name.length
        var b = true
        logger.info("[Notice:PlayerList] A new player joined: ${player.name} @${player.hashCode()}")
        playerList.forEachIndexed { index, s ->
            val tl = s.name.length
            if (tl < length) return
            else {
                playerList.add(index, player)
                b = false
                logger.info("[Notice:PlayerList] The name length is $tl, at index $index")
                return@forEachIndexed
            }
        }
        if (b) {
            playerList.add(player)
            logger.info("[Notice:PlayerList] The name length is $length, at index 0")
        }
        maxLength = playerList.last().name.length
        logger.info("[Notice:PlayerList] Player list updated")
    }

    private fun playerUpdateRemove(player: Player) {
        return
        logger.info("[Notice:PlayerList] A player quited: ${player.name} @${player.hashCode()}")
        playerList.forEachIndexed { index, p ->
            if (p == player) playerList.removeAt(index)
            logger.info("[Notice:PlayerList] Player removed")
        }
        try {
            maxLength = playerList.last().name.length
        } catch (e: NoSuchElementException) {
            maxLength = 0
        }
        logger.info("[Notice:PlayerList] Player list updated")
    }

}
