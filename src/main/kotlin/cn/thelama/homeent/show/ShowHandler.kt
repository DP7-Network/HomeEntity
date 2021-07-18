package cn.thelama.homeent.show

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.secure.AuthHandler
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta


object ShowHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(!AuthHandler.getLoginState(sender.uniqueId)) {
                return true
            }
            if(args.isNotEmpty()) {
                when(args[0]) {
                    "helmet" -> {
                        display(sender.inventory.helmet, sender)
                    }

                    "chestplate" -> {
                        display(sender.inventory.chestplate,sender)
                    }

                    "leggings" -> {
                        display(sender.inventory.leggings, sender)
                    }

                    "boots" -> {
                        display(sender.inventory.boots, sender)
                    }

                    "offhand" -> {
                        display(sender.inventory.itemInOffHand, sender)
                    }

                    "inv" -> {
                        //TODO
                    }

                    "byid" -> {
                        if(args.size > 1) {
                            ShowManager.show(args[1], sender)
                        }
                    }

                    else -> {
                        sender.sendMessage("/show [helmet|chestplate|leggings|boots|offhand|inv]")
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

        val base = ComponentBuilder("${ChatColor.GREEN}${sender.name} 展示了他的 ")
        val msg = ComponentBuilder("${ChatColor.AQUA}[${parseMetaName(item.itemMeta, CraftItemStack.asNMSCopy(item))}]")
        msg.currentComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/show byid ${ShowManager.createDisplay(item)}")
        val d = base.append(msg.create()).create()
        Bukkit.getOnlinePlayers().forEach { it.spigot().sendMessage(*d) }
    }

    private fun parseMetaName(meta: ItemMeta?, nmsItem: net.minecraft.world.item.ItemStack): String {
        if(meta == null) {
            return "${ChatColor.RED}Unit${ChatColor.AQUA}"
        }
        return if(meta.hasDisplayName()) {
            meta.displayName
        } else {
            HomeEntity.instance.minecraftTranslation[nmsItem.item.name]!!
        }
    }
}