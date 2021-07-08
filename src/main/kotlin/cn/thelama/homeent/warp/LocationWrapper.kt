package cn.thelama.homeent.warp

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.util.*

data class LocationWrapper(
    val world: UUID,
    val x: Double,
    val y: Double,
    val z: Double
) {
    fun toLoc(): Location {
        return Location(Bukkit.getWorld(world), x, y, z)
    }
    fun toLoc(entity: LivingEntity): Location {
        val eyeLocation = entity.eyeLocation
        return Location(Bukkit.getWorld(world), x, y, z, eyeLocation.yaw, eyeLocation.pitch)
    }
}