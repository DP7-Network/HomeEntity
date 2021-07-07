package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.module.ModuleCommand
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.module.PlayerDataProvider
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.NumberFormatException
import kotlin.math.ceil

object WarpHandlerV2 : CommandExecutor, ModuleCommand, PlayerDataProvider<MutableMap<String, LocationEntry>?> {
    private val warps: MutableMap<UUID, MutableMap<String, LocationEntry>> = ModuledPlayerDataManager.getAllTyped("warp")
    private val ops = listOf("set", "rm", "list", "lookup", "desc", "share", "find")

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage("${ChatColor.RED}抱歉, 该指令专属于玩家")
            return true
        }

        if(args.isEmpty()) {
            sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            return true
        }
        if(sender.uniqueId !in warps) {
            warps[sender.uniqueId] = hashMapOf()
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
                                warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid,
                                    args[2].toDouble(), args[3].toDouble(), args[4].toDouble()), args[5])

                            } else {
                                warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid,
                                    args[2].toDouble(), args[3].toDouble(), args[4].toDouble()), "")
                            }
                            sender.sendMessage("已创建地标 ${ChatColor.GOLD}${args[1]}${ChatColor.RESET}")
                        }.onFailure {
                            if (it is NumberFormatException) {
                                val message = it.message!!
                                val err = message.substring(19, message.length - 2)
                                sender.sendMessage("${ChatColor.YELLOW}$err${ChatColor.RED} 不是一个有效数字")
                            } else throw it;
                        }
                    }
                    else -> {
                        if(!checkName(args[1], sender)) {
                            return true
                        }

                        if(args.size > 2) {
                            warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid,
                                sender.location.x, sender.location.y, sender.location.z), args[2])

                        } else {
                            warps[sender.uniqueId]!![args[1]] = LocationEntry(LocationWrapper(sender.world.uid,
                                sender.location.x, sender.location.y, sender.location.z), "")
                        }
                        sender.sendMessage("已创建地标: '${ChatColor.GOLD}${args[1]}'")
                    }
                }
            }
            "rm" -> {
                if(args.size > 1) {
                    warps[sender.uniqueId]!!.remove(args[1])
                } else {
                    sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                }
            }
            "list" -> {
                val num = 7f //每页显示数量 (一定加f)

                val tmpWarps = warps[sender.uniqueId]!!
                if (tmpWarps.isEmpty()) {
                    sender.sendMessage("${ChatColor.GOLD}没有可显示的地标")
                    return true
                }

                val allPagesNumber = ceil(tmpWarps.size / num) //-> The number of all pages.

                fun showList(currentPage: Int) {
                    // 这是第 n 页, 共 s 页 [上一页(pn)] [下一页(nn)]
                    // name: x y z | [分享] [删除] [设为当前位置]
                    // 使用 /warp list [页数] 来查看相应页数
                    sender.spigot().sendMessage(*ComponentBuilder(
                                "${ChatColor.GOLD}这是第 ${ChatColor.YELLOW}$currentPage${ChatColor.GOLD} 页, " +
                                "共 ${ChatColor.YELLOW}$allPagesNumber${ChatColor.GOLD} 页 ")
                        .append(ComponentBuilder(
                            "${ChatColor.GOLD}[${ChatColor.YELLOW}${ChatColor.UNDERLINE}上一页"
                        ).create()).create())

                }

                if (args.size > 1) {
                    val currentPage: Int //-> Current page number to list.
                    try {
                        currentPage = args[1].toInt()
                    } catch (_: NumberFormatException) {
                        sender.sendMessage("${ChatColor.YELLOW}${args[1]}${ChatColor.RED} 不是一个有效数字")
                        return true
                    }
                    //Send an error if specified page is out of the number of all pages.
                    if (currentPage > allPagesNumber) {
                        sender.sendMessage("${ChatColor.RED}页数 ${ChatColor.YELLOW}${args[1]}${ChatColor.RED} 过大")
                        return true
                    }
                    showList(currentPage)
                }
                else showList(1);
            }
            "lookup" -> {
            }
            "desc" -> {
            }
            "share" -> {
            }
            "find" -> {
            }
        }
        return true
    }

    private fun checkName(name: String, sender: CommandSender): Boolean {
        if(name in ops) {
            sender.sendMessage("${ChatColor.RED}'$name'不可以做为地标的名称, 更多内容请查看帮助")
            return false
        }
        return true
    }

    override fun save() {
        ModuledPlayerDataManager.setAllTyped("warp", warps)
    }

    override fun config(uuid: UUID): MutableMap<String, LocationEntry>? = warps[uuid]
}