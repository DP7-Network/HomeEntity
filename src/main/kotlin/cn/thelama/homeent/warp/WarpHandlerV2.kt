package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.module.ModuleCommand
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.module.PlayerDataProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object WarpHandlerV2 : CommandExecutor, ModuleCommand, PlayerDataProvider<MutableMap<String, LocationEntry>?> {
    private val warps: MutableMap<UUID, MutableMap<String, LocationEntry>> = ModuledPlayerDataManager.getAllTyped("warp")
    private val ops = listOf("set", "rm", "list", "lookup", "desc", "share", "find")

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage("${ChatColor.RED}只有玩家才可以执行本指令")
            return true
        }

        if(args.isEmpty()) {
            sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            return true
        }

        when(args[0]) {
            "set" -> {
                when {
                    args.size < 2 -> {
                        sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                    }
                    args.size > 4 -> {
                        runCatching {
                            if(!checkName(args[1], sender)) {
                                return true
                            }
                            if(args.size > 5) {
                                warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid, args[2].toDouble(), args[3].toDouble(), args[4].toDouble()), args[5])

                            } else {
                                warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid, args[2].toDouble(), args[3].toDouble(), args[4].toDouble()), "")
                            }
                            sender.sendMessage("${ChatColor.GREEN}成功创建了地标: '${ChatColor.GOLD}${args[1]}${ChatColor.RESET}'")
                        }.onFailure {
                            sender.sendMessage("${ChatColor.RED}无法将您输入得参数转换为Double型数字")
                        }
                    }
                    else -> {
                        if(!checkName(args[1], sender)) {
                            return true
                        }
                        if(sender.uniqueId !in warps) {
                            warps[sender.uniqueId] = hashMapOf()
                        }

                        if(args.size > 2) {
                            warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid, sender.location.x, sender.location.y, sender.location.z), args[2])

                        } else {
                            warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid, sender.location.x, sender.location.y, sender.location.z), "")
                        }
                        sender.sendMessage("${ChatColor.GREEN}成功创建了地标: '${ChatColor.GOLD}${args[1]}${ChatColor.RESET}'")
                    }
                }
            }
            "rm" -> {
                if(args.size > 1) {
                    if(sender.uniqueId !in warps) {
                        warps[sender.uniqueId] = hashMapOf()
                    }
                    warps[sender.uniqueId]!!.remove(args[1])
                } else {
                    sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                }
            }
            "list" -> {
                // <记录点> [分享] [删除] [修改到当前位置]
                //当前页 <Page> 共 <Page> [下一页(可能没有)] [上一页(可能没有)]
            }
        }
        return true
    }

    private fun checkName(name: String, sender: CommandSender): Boolean {
        if(name in ops) {
            sender.sendMessage("${ChatColor.RED}'$name'不可以做为地标的名称! ${ops.joinToString(" ") { "'$it'" }} 都不可以做为地标的名称!")
            return false
        }
        return true
    }

    override fun save() {
        ModuledPlayerDataManager.setAllTyped("warp", warps)
    }

    override fun config(uuid: UUID): MutableMap<String, LocationEntry>? = warps[uuid]
}