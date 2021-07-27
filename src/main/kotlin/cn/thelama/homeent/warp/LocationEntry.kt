//@file:JvmName("LocationEntry")

package cn.thelama.homeent.warp

import org.bukkit.Location
import org.bukkit.entity.LivingEntity

data class LocationEntry(@JvmField var name: String, @JvmField var x: Double, @JvmField var y: Double, @JvmField var z: Double, @JvmField var world: Int, @JvmField var description: String) {
    constructor() : this("", 0.0, 0.0, 0.0, 0 , "") {}
}

fun LocationEntry.createLocation(entity: LivingEntity) = Location(GameWorld.toWorld(world) ?: GameWorld.overworld, x, y, z)

fun LocationEntry.getWorld() = GameWorld.toWorld(world)