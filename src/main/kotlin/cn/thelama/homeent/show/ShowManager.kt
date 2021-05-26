package cn.thelama.homeent.show

import cn.thelama.homeent.HomeEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

object ShowManager : Listener {
    private val inventories = mutableMapOf<String, Inventory>()

    fun createDisplay(item: ItemStack): String {
        val inv = Bukkit.createInventory(null, 9, "${ChatColor.GREEN} 物品展示")
        inv.setItem(4, item)
        val identifier = (item.hashCode() + (floor(Math.random() * 10))).toString()
        inventories[identifier] = inv
        Bukkit.getScheduler().runTaskLater(HomeEntity.instance, Runnable {
            inventories.remove(identifier)
        }, 20 * 300)
        return identifier
    }

    fun show(identifier: String, target: Player) {
        if(inventories.containsKey(identifier)) {
            target.openInventory(inventories[identifier]!!)
        }
    }

    @EventHandler
    fun avoidInvActions(e: InventoryClickEvent) {
        if(e.inventory in inventories.values) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun avoidInvActions(e: InventoryDragEvent) {
        if(e.inventory in inventories.values) {
            e.isCancelled = true
        }
    }
}