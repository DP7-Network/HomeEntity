package cn.thelama.homeent.tpa

import cn.thelama.homeent.HomeEntity
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

object TPManager {
    private val tpRequestsGo: HashMap<UUID, MutableList<UUID>> = hashMapOf()
    private val tpRequestsHere: HashMap<UUID, MutableList<UUID>> = hashMapOf()

    fun newRequestGo(from: Player, to: Player) {
        synchronized(tpRequestsGo) {
            if(tpRequestsGo.containsKey(to.uniqueId)) {
                tpRequestsGo[to.uniqueId]!! += from.uniqueId
            } else {
                tpRequestsGo[to.uniqueId] = mutableListOf(from.uniqueId)
            }
        }

        from.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}传送请求已发送")
        val base = ComponentBuilder("${ChatColor.GOLD}${from.name} 请求传送到您身边 ")
        val accept = ComponentBuilder("${ChatColor.GRAY}[${ChatColor.GREEN}接受${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept go ${from.uniqueId}")
        val deny = ComponentBuilder(" ${ChatColor.GRAY}[${ChatColor.RED}拒绝${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny go ${from.uniqueId}")
        base.append(accept.create())
        base.append(deny.create())
        to.spigot().sendMessage(*base.create())

        Bukkit.getScheduler().runTaskLater(HomeEntity.instance, Runnable {
            synchronized(tpRequestsGo) {
                if(tpRequestsGo.containsKey(to.uniqueId)) {
                    tpRequestsGo[to.uniqueId]!! -= from.uniqueId
                }
            }
        }, 60 * 20)
    }

    fun newRequestHere(from: Player, to: Player) {
        synchronized(tpRequestsHere) {
            if(tpRequestsHere.containsKey(to.uniqueId)) {
                tpRequestsHere[to.uniqueId]!! += from.uniqueId
            } else {
                tpRequestsHere[to.uniqueId] = mutableListOf(from.uniqueId)
            }
        }

        from.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}传送请求已发送")
        val base = ComponentBuilder("${ChatColor.GOLD}${from.name} 请求传送他身边 ")
        val accept = ComponentBuilder("${ChatColor.GRAY}[${ChatColor.GREEN}接受${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept here ${from.uniqueId}")
        val deny = ComponentBuilder(" ${ChatColor.GRAY}[${ChatColor.RED}拒绝${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny here ${from.uniqueId}")
        base.append(accept.create())
        base.append(deny.create())
        to.spigot().sendMessage(*base.create())

        Bukkit.getScheduler().runTaskLater(HomeEntity.instance, Runnable {
            synchronized(tpRequestsHere) {
                if(tpRequestsHere.containsKey(to.uniqueId)) {
                    tpRequestsHere[to.uniqueId]!! -= from.uniqueId
                }
            }
        }, 60 * 20)
    }

    fun acceptHere(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(tpRequestsHere.containsKey(to.uniqueId)) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                    val p = Bukkit.getPlayer(uuid) ?: return
                    to.teleport(p)
                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }   
        }
    }

    fun acceptGo(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(tpRequestsHere.containsKey(to.uniqueId)) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                    Bukkit.getPlayer(uuid)?.teleport(to)
                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }
        }
    }

    fun denyHere(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(tpRequestsHere.containsKey(to.uniqueId)) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                    Bukkit.getPlayer(uuid)?.sendMessage("${ChatColor.RED}您向 ${to.name} 的传送请求已被拒绝")
                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }
        }
    }

    fun denyGo(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(tpRequestsHere.containsKey(to.uniqueId)) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                    Bukkit.getPlayer(uuid)?.sendMessage("${ChatColor.RED}您向 ${to.name} 的传送请求已被拒绝")
                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }
        }
    }
}