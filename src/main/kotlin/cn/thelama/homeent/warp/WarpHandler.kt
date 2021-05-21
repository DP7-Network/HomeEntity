package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

object WarpHandler : CommandExecutor {
    private val ops = listOf("add", "del", "list", "lookup", "save")
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "warp") {
            if(sender is Player) {
                if(!HomeEntity.instance.isLogin(sender)) {
                    return true
                }
                if(args.isNotEmpty()) {
                    when(args[0]) {
                        "add" -> {
                            if(args.size < 2) {
                                sender.sendMessage("${ChatColor.RED}Illegal arguments! /warp add <name> [<x> <y> <z>]")
                            } else if (args.size > 4) {
                                runCatching {
                                    if(!checkName(args[1], sender)) {
                                        sender.sendMessage("${ChatColor.RED}Failed to create a warp point!")
                                        return true
                                    }
                                    HomeEntity.instance.warps[args[1]] = LocationWrapper(sender.world.uid, args[2].toDouble(), args[3].toDouble(), args[4].toDouble())
                                    sender.sendMessage("${ChatColor.GREEN}Successfully created warp: '${args[1]}'")
                                }.onFailure {
                                    sender.sendMessage("${ChatColor.RED}Failed to parse arguments to a double number!")
                                }
                            } else {
                                if(!checkName(args[1], sender)) {
                                    sender.sendMessage("${ChatColor.RED}Failed to create warp point!")
                                    return true
                                }
                                HomeEntity.instance.warps[args[1]] = LocationWrapper(sender.world.uid, sender.location.x, sender.location.y, sender.location.z)
                                sender.sendMessage("${ChatColor.GREEN}Successfully created warp: '${args[1]}'")
                            }
                        }

                        "del" -> {
                            if(args.size > 1) {
                                HomeEntity.instance.warps.remove(args[1])
                                sender.sendMessage("${ChatColor.GREEN}Deleted!")
                            } else {
                                sender.sendMessage("${ChatColor.RED}Illegal arguments! /warp del <name>")
                            }
                        }

                        "list" -> {
                            sender.sendMessage("${ChatColor.GREEN}Server warps: ")
                            HomeEntity.instance.warps.forEach { (name, loc) ->
                                sender.sendMessage("  $name is at '${loc.toLoc().world?.name}' x: ${floor(loc.x)} y: ${floor(loc.y)} z: ${floor(loc.z)}")
                            }
                        }

                        "lookup" -> {
                            if(args.size < 2) {
                                sender.sendMessage("${ChatColor.RED}Illegal arguments! /warp lookup <name>")
                            } else {
                                if(HomeEntity.instance.warps.containsKey(args[1])) {
                                    val loc = HomeEntity.instance.warps[args[1]]!!
                                    sender.sendMessage("${ChatColor.GREEN}${args[1]} is at '${loc.toLoc().world?.name}' x: ${floor(loc.x)} y: ${floor(loc.y)} z: ${floor(loc.z)}")
                                } else {
                                    sender.sendMessage("${ChatColor.RED}${args[1]} Dose not exists!")
                                }
                            }
                        }

                        else -> {
                            if(HomeEntity.instance.warps.containsKey(args[0])) {
                                if(args[0].startsWith("lama.") && sender.name != "Lama3L9R") {
                                    sender.sendMessage("${ChatColor.RED}Unable to find warp point '${args[0]}'")
                                    return true
                                }
                                sender.location.world?.playSound(sender.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                                sender.location.world?.playEffect(sender.location, Effect.ENDER_SIGNAL, 4)
                                sender.teleport(HomeEntity.instance.warps[args[0]]!!.toLoc())
                                sender.location.world?.playSound(sender.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                                sender.location.world?.playEffect(sender.location, Effect.ENDER_SIGNAL, 4)
                            } else {
                                sender.sendMessage("${ChatColor.RED}Unable to find warp point '${args[0]}'")
                            }
                        }
                    }

                }
            } else {
                sender.sendMessage("${ChatColor.RED}Only player can run this command")
            }
        }
        return true
    }

    private fun checkName(name: String, sender: CommandSender): Boolean {
        if(name.contains(",")) {
            sender.sendMessage("${ChatColor.RED}Illegal Character ','! You can't use ',' as a warp point name!")
            return false
        }
        if(ops.contains(name)) {
            sender.sendMessage("${ChatColor.RED}Illegal Name '$name'! You can't use following names: add, del, list, lookup, save")
            return false
        }
        return true
    }
}