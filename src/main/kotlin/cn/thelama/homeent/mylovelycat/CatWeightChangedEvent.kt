package cn.thelama.homeent.mylovelycat

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CatWeightChangedEvent(var increaseWeight: Double, val befor: Double, val after: Double) : Event(true), Cancellable {
    companion object {
        private val handlerList = HandlerList()
    }

    private var isCancelled: Boolean = false

    override fun getHandlers() = handlerList

    override fun isCancelled() = isCancelled

    override fun setCancelled(p0: Boolean) {
        this.isCancelled = true
    }
}