package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

object GameWorld {
    val overworld: World = Bukkit.getWorld(HomeEntity.theServer.dedicatedServerProperties.p)!!
    val nether: World?
    val theEnd: World?

    init {
        if(HomeEntity.theServer.dedicatedServerProperties.A) {
            nether = Bukkit.getWorld("${overworld.name}_nether")
            theEnd = Bukkit.getWorld("${overworld.name}_the_end")
        } else {
            nether = null
            theEnd = null
        }
    }

    fun toWorld(i: Int): World? {
        return when(i) {
            0 -> overworld
            1 -> nether
            2 -> theEnd
            else -> null
        }
    }

    fun toConfigurationID(uid: UUID): Int {
        return when(uid) {
            overworld.uid -> 0
            nether?.uid -> 1
            theEnd?.uid -> 2
            else -> -1
        }
    }
}