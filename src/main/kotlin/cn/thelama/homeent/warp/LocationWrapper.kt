package cn.thelama.homeent.warp

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*


data class LocationWrapper(val world: UUID, val x: Double, val y: Double, val z: Double) {

    fun toLoc(): Location {
        return Location(Bukkit.getWorld(world), x, y, z)
    }
}