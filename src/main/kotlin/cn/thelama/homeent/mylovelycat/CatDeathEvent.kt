package cn.thelama.homeent.mylovelycat

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CatDeathEvent(val lastWeightChange: Double, val befor: Double, val after: Double, var next: Double, val reset: Boolean = false) : Event(true) {
    companion object {
        private val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList


}