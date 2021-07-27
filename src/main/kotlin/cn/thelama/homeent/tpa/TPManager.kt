package cn.thelama.homeent.tpa

import cn.thelama.homeent.HomeEntity
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

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

        val base = ComponentBuilder("${ChatColor.GOLD}${from.name} 请求传送到您身边 ")
        val accept = ComponentBuilder("${ChatColor.GRAY}[${ChatColor.GREEN}接受${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept go ${from.uniqueId}")
        accept.currentComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.GREEN}同意${ChatColor.AQUA}将 ${ChatColor.WHITE}${from.name} ${ChatColor.AQUA}传送到您身边的请求"))

        val deny = ComponentBuilder(" ${ChatColor.GRAY}[${ChatColor.RED}拒绝${ChatColor.GRAY}]")
        deny.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny go ${from.uniqueId}")
        deny.currentComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.RED}拒绝${ChatColor.AQUA}将 ${ChatColor.WHITE}${from.name} ${ChatColor.AQUA}传送到您身边的请求"))
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

        val base = ComponentBuilder("${ChatColor.GOLD}${from.name} 请求传送他身边 ")
        val accept = ComponentBuilder("${ChatColor.GRAY}[${ChatColor.GREEN}接受${ChatColor.GRAY}]")
        accept.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept here ${from.uniqueId}")
        accept.currentComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.GREEN}同意${ChatColor.AQUA}将您传送至 ${ChatColor.WHITE}${from.name}"))
        val deny = ComponentBuilder(" ${ChatColor.GRAY}[${ChatColor.RED}拒绝${ChatColor.GRAY}]")
        deny.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny here ${from.uniqueId}")
        deny.currentComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("${ChatColor.RED}拒绝${ChatColor.AQUA}传送到 ${ChatColor.WHITE}${from.name} ${ChatColor.AQUA}的请求"))

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
    // Teleport to to uuid
    fun acceptHere(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(to.uniqueId in tpRequestsHere) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                    val player = Bukkit.getPlayer(uuid) ?: return

                    val cost = ceil(sqrt((to.location.x - player.location.x).pow(2.0) + (to.location.z - player.location.z).pow(2.0)) / 100)
                    if(player.health > cost) {
                        player.damage(cost)
                        to.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}传送中")
                        to.teleport(player)
                    } else {
                        player.sendMessage("${ChatColor.RED}无法传送! 您当前的血量不足以到达 ${to.name} 的位置! 您的血量: ${floor(player.health)}❤ 需要血量: $cost❤")
                    }

                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }   
        }
    }

    // Teleport uuid to to
    fun acceptGo(to: Player, uuid: UUID) {
        synchronized(tpRequestsGo) {
            if(to.uniqueId in tpRequestsGo) {
                if(uuid in tpRequestsGo[to.uniqueId]!!) {
                    Bukkit.getPlayer(uuid)?.also {
                        val cost = ceil(sqrt((to.location.x - it.location.x).pow(2.0) + (to.location.z - it.location.z).pow(2.0)) / 100)
                        if(it.health > cost) {
                            it.damage(cost)
                            it.teleport(to)
                            it.sendMessage("${ChatColor.GOLD}${ChatColor.ITALIC}传送中")
                        } else {
                            it.sendMessage("${ChatColor.RED}无法传送! 您当前的血量不足以到达 ${to.name} 的位置! 您的血量: ${floor(it.health)}❤ 需要血量: $cost❤")
                        }
                    }
                    tpRequestsGo[to.uniqueId]!! -= uuid
                }
            }
        }
    }

    fun denyHere(to: Player, uuid: UUID) {
        synchronized(tpRequestsHere) {
            if(to.uniqueId in tpRequestsHere) {
                if(uuid in tpRequestsHere[to.uniqueId]!!) {
                   to.sendMessage("${ChatColor.RED}您向 ${Bukkit.getPlayer(uuid)?.name ?: "[UNKNOWN]"} 的传送请求已被拒绝")
                    tpRequestsHere[to.uniqueId]!! -= uuid
                }
            }
        }
    }

    fun denyGo(to: Player, uuid: UUID) {
        synchronized(tpRequestsGo) {
            if(to.uniqueId in tpRequestsGo) {
                if(uuid in tpRequestsGo[to.uniqueId]!!) {
                    Bukkit.getPlayer(uuid)?.sendMessage("${ChatColor.RED}您向 ${to.name} 的传送请求已被拒绝")
                    tpRequestsGo[to.uniqueId]!! -= uuid
                }
            }
        }
    }
}