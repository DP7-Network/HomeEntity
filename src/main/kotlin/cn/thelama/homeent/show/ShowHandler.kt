package cn.thelama.homeent.show

import cn.thelama.homeent.HomeEntity
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.hover.content.Item
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta


object ShowHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(!HomeEntity.instance.isLogin(sender)) {
                return true
            }
            if(args.isNotEmpty()) {
                when(args[0]) {
                    "head" -> {
                        display(sender.inventory.helmet, sender)
                    }

                    "body" -> {
                        display(sender.inventory.chestplate,sender)
                    }

                    "leg" -> {
                        display(sender.inventory.leggings, sender)
                    }

                    "foot" -> {
                        display(sender.inventory.boots, sender)
                    }

                    "left" -> {
                        display(sender.inventory.itemInOffHand, sender)
                    }

                    "inv" -> {
                        //TODO
                    }

                    else -> {
                        sender.sendMessage("/show [head|body|leg|foot|left|inv]")
                    }
                }
            } else {
                display(sender.inventory.itemInMainHand, sender)
            }
        }
        return true
    }

    private fun display(item: ItemStack?, sender: Player) {
        if(item == null) {
            Bukkit.broadcastMessage("${sender.name} Showed his air")
            return
        }

        val nbt = NBTTagCompound()
        val nmsItem = CraftItemStack.asNMSCopy(item).also { it.save(nbt) }
        val base = ComponentBuilder("${ChatColor.GREEN}${sender.name} Showed his ")
        val msg = ComponentBuilder("${ChatColor.AQUA}[${parseMetaName(item.itemMeta)}]")
        val name = nmsItem.item.name.replace("block.", "").replace("item.", "").replace(".", ":")
        msg.currentComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(name, 1, ItemTag.ofNbt(nbt.toString())))
        base.append(msg.create())
        Bukkit.getOnlinePlayers().forEach { it.spigot().sendMessage(*base.create()) }
    }

    private fun parseMetaName(meta: ItemMeta?): String {
        if(meta == null) {
            return "${ChatColor.RED}Unit${ChatColor.AQUA}"
        }
        return if(meta.hasDisplayName()) {
            meta.displayName
        } else {
            meta.localizedName
        }
    }
}