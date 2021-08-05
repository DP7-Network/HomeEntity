package cn.thelama.homeent

import cn.thelama.homeent.warp.GameWorld
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object BossBarTips : Listener {
    private val bbNamespacedKey = NamespacedKey(HomeEntity.instance, "tips_bb")
    private val bar = Bukkit.getBossBar(bbNamespacedKey) ?: Bukkit.createBossBar(bbNamespacedKey, "HomeEntity Loading...", BarColor.BLUE, BarStyle.SOLID)
    private val task = Bukkit.getScheduler().runTaskTimer(HomeEntity.instance, Runnable {
        val hr = (GameWorld.overworld.time / 1000) + 8
        val min = (GameWorld.overworld.time % 1000) / 16
        if(hr in 6..17) {
            bar.setTitle("${ChatColor.GOLD}欢迎来到Charmless! 当前时间: ${ChatColor.GREEN} ${if(hr > 12) { "${(hr - 12).toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} pm" } else { "${hr.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} am" } }")
        } else {
            bar.setTitle("${ChatColor.GOLD}欢迎来到Charmless! 当前时间: ${ChatColor.LIGHT_PURPLE} ${if(hr > 12) { "${(hr - 12).toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} pm" } else { "${hr.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} am" } }")
        }
    }, 0, 17)

    fun init() {
        Bukkit.getOnlinePlayers().forEach {
            bar.addPlayer(it)
        }
    }

    fun shutdown() {
        task.cancel()
        bar.removeAll()
    }

    fun addPlayer(p: Player) {
        bar.addPlayer(p)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        bar.removePlayer(e.player)
    }
}