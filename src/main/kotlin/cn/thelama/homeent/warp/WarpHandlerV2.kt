package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.module.ModuleCommand
import cn.thelama.homeent.module.ModuledPlayerDataManager
import cn.thelama.homeent.module.PlayerDataProvider
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.NumberFormatException
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

object WarpHandlerV2 : CommandExecutor, ModuleCommand, PlayerDataProvider<MutableMap<String, LocationEntry>?> {
    private val warps = getWarps()
    val ops = listOf("set", "rm", "list", "detail", "set-des", "share", "find")

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
            warps[sender.uniqueId] = linkedMapOf()
        }

        when(args[0]) {
            // warp set <name> [x y z] [force]
            "set" -> {
                val tmpMap = warps[sender.uniqueId]!!

                fun warn(command: String) {
                    val confirmButton = ComponentBuilder(
                        "${ChatColor.GOLD}${ChatColor.UNDERLINE}点击这里${ChatColor.RESET}")
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .event(TextComponent("${ChatColor.RED}该操作不可逆").hoverEvent)
                        .create()
                    sender.sendMessage("${ChatColor.RED}地标名称 ${ChatColor.GOLD}${args[1]}${ChatColor.RED} 已存在, " +
                            "若继续设置则将覆盖原有地标")
                    sender.spigot().sendMessage(*ComponentBuilder(
                        "${ChatColor.YELLOW}请在最后添加 ${ChatColor.GOLD}force${ChatColor.YELLOW} 参数或")
                        .append(confirmButton).append("${ChatColor.YELLOW}确认").create())
                }

                when {
                    args.size < 2 -> {
                        sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                    }
                    args.size > 4 -> {
                        runCatching {
                            if (!checkName(args[1], sender)) {
                                return true
                            }
                            if (args.size > 5 && args[5] == "force" || !tmpMap.containsKey(args[1])) {
                                tmpMap[args[1]] = LocationEntry(args[1], LocationWrapper(sender.world.uid,
                                        args[2].toDouble(), args[3].toDouble(), args[4].toDouble()), "")
                                sender.sendMessage("已创建地标 ${ChatColor.GOLD}${args[1]}")
                            }
                            else warn("/warp set ${args[1]} ${args[2]} ${args[3]} ${args[4]} force")
                        }.onFailure {
                            if (it is NumberFormatException) {
                                val message = it.message!!
                                val err = message.substring(19, message.length - 2)
                                sender.sendMessage("${ChatColor.YELLOW}$err${ChatColor.RED} 不是一个有效数字")
                            } else throw it
                        }
                    }
                    else -> {
                        if(!checkName(args[1], sender)) {
                            return true
                        }
                        if (args.size > 3 && args[2] == "force" || !tmpMap.containsKey(args[1])) {
                            tmpMap[args[1]] = LocationEntry(
                                args[1], LocationWrapper(sender.world.uid,
                                    sender.location.x, sender.location.y, sender.location.z), ""
                            )
                            sender.sendMessage("已创建地标 ${ChatColor.GOLD}${args[1]}")
                        }
                        else warn("/warp set ${args[1]} force")
                    }
                }
            }

            // warp rm <name>
            "rm" -> {
                if(args.size > 1) {
                    if (args.size > 2 && args[2] == "force") {
                        warps[sender.uniqueId]!!.remove(args[1])
                        sender.sendMessage("已删除地标 ${ChatColor.GOLD}${args[1]}")
                    } else {
                        val confirmButton = ComponentBuilder(
                            "${ChatColor.GOLD}${ChatColor.UNDERLINE}点击这里${ChatColor.RESET}")
                            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp rm ${args[1]} force"))
                            .event(TextComponent("${ChatColor.RED}该操作不可逆").hoverEvent)
                            .create()
                        sender.sendMessage("${ChatColor.RED}确定要删除地标 ${ChatColor.GOLD}${args[1]}" +
                                "${ChatColor.RED} 吗, 该地标将会永久失去! (真的很久!)")
                        sender.spigot().sendMessage(*ComponentBuilder(
                            "${ChatColor.YELLOW}请在最后添加 ${ChatColor.GOLD}force${ChatColor.YELLOW} 参数或")
                            .append(confirmButton).append("${ChatColor.YELLOW}确认").create())
                    }
                } else {
                    sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
                }
            }

            // warp list [page]
            "list" -> {
                var num = 7f //每页显示数量 (一定加f)

                val tmpWarps = warps[sender.uniqueId]!!
                if (tmpWarps.isEmpty()) {
                    sender.sendMessage("${ChatColor.GOLD}没有可显示的地标")
                    return true
                }

                var allPagesNumber = 1 //-> The number of all pages.

                fun showList(currentPage: Int) {
                    // name: x y z | [分享] [删除] [设为当前位置]
                    // 这是第 n 页, 共 s 页 [上一页(pn)] [下一页(nn)]
                    // 使用 /warp list [页数] 来查看相应页数

                    /* 第一部分: 遍历地标并逐行显示 */
                    val lastIndex = currentPage * num - 1
                    val firstIndex = lastIndex - num + 1
                    //已确认需要修改数据结构，MutableMap是无序的, 可能每次遍历的顺序都不同(目前改为 `LinkedHashMap` ).
                    //同时原数据结构遍历时无法获取地标名称, 故无法直接根据点击的操作执行相应地标的指令, 因此在
                    //LocationEntry 中添加了 name:String 属性, 以方便直接获取地标名称;
                    //但这样导致存储时数据多余(会存储两个 name 属性), 因此存储时改为直接存储一个 `List<LocationEntry>` ,
                    //加载时将该列表重新处理为 LinkedHashMap 从而做到不影响其他功能.
                    //(这种数据结构相较于之前, 多了warp点数量的引用和玩家数量的 HashMap 对象, 其余不变,
                    //故几乎不会占用更多内存, 只是加载和保存时处理时间会增加.)
                    tmpWarps.values.forEachIndexed { index, entry ->
                        if (index > lastIndex) return@forEachIndexed
                        if (index >= firstIndex) {
                            sender.spigot().sendMessage(*ComponentBuilder("$index. ")
                                .append(getWarpInfo(entry)).create())
                        }
                    }

                    /* 第二部分: 显示页数信息 */
                    val pageButton1 =
                        if (currentPage == 1)
                            ComponentBuilder(
                                "${ChatColor.GOLD}[${ChatColor.DARK_GRAY}已经是第一页了${ChatColor.GOLD}]").create()
                        else {
                            val previousPage = currentPage - 1
                            ComponentBuilder(
                                "${ChatColor.GOLD}[${ChatColor.YELLOW}${ChatColor.UNDERLINE}上一页($previousPage)${ChatColor.GOLD}]")
                                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp list $previousPage")).create()
                        }
                    val pageButton2 =
                        if (currentPage == allPagesNumber)
                            ComponentBuilder(
                                "${ChatColor.GOLD}[${ChatColor.DARK_GRAY}已经是最后一页了${ChatColor.GOLD}]").create()
                        else {
                            val nextPage = currentPage + 1
                            ComponentBuilder(
                                "${ChatColor.GOLD}[${ChatColor.YELLOW}${ChatColor.UNDERLINE}下一页($nextPage)${ChatColor.GOLD}]")
                                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp list $nextPage")).create()
                        }

                    sender.spigot().sendMessage(*ComponentBuilder(
                                "${ChatColor.GOLD}这是第 ${ChatColor.YELLOW}$currentPage${ChatColor.GOLD} 页, " +
                                "共 ${ChatColor.YELLOW}$allPagesNumber${ChatColor.GOLD} 页 ")
                        .append(pageButton1).append(" ").append(pageButton2).create())

                    /* 第三部分: 显示命令提示 */
                    val commandTip = ComponentBuilder("${ChatColor.GRAY}/warp list <页数>${ChatColor.RESET}")
                        .event(TextComponent("点击输入").hoverEvent)
                        .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp list "))
                        .create()

                    sender.spigot().sendMessage(
                        *ComponentBuilder("使用 ").append(commandTip).append(" 来查看相应页数").create())
                }

                if (args.size > 1) {
                    val currentPage: Int //-> Current page number to list.
                    try {
                        currentPage = args[1].toInt()
                        allPagesNumber = ceil(tmpWarps.size / num).toInt()
                    } catch (_: NumberFormatException) {
                        if (args[1] == "all") {
                            num = tmpWarps.size.toFloat()
                            showList(1)
                        } else {
                            sender.sendMessage("${ChatColor.YELLOW}${args[1]}${ChatColor.RED} 不是一个有效数字")
                        }
                        return true
                    }
                    //Send an error if specified page is out of the number of all pages.
                    if (currentPage > allPagesNumber) {
                        sender.sendMessage("${ChatColor.RED}页数 ${ChatColor.YELLOW}${args[1]}${ChatColor.RED} 过大")
                        return true
                    }
                    showList(currentPage)
                }
                else showList(1)
            }

            // warp detail <name>
            "detail" -> {
                if (args.size > 1) {
                    val entry = warps[sender.uniqueId]!![args[1]]
                    if (entry == null) {
                        sender.sendMessage("${ChatColor.RED}地标 ${ChatColor.GOLD}${args[1]}${ChatColor.RED} 不存在")
                    } else {
                        val location = entry.location
                        val name = entry.name
                        val x = floor(location.x)
                        val y = floor(location.y)
                        val z = floor(location.z)
                        val buttonShare =
                            ComponentBuilder("${ChatColor.AQUA}${ChatColor.UNDERLINE}分享地标${ChatColor.RESET}")
                                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp share $name"))
                                .event(TextComponent("点击将地标信息发送给所有人").hoverEvent)
                                .create()
                        val buttonDelete =
                            ComponentBuilder("${ChatColor.RED}${ChatColor.UNDERLINE}删除地标${ChatColor.RESET}")
                                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp rm $name"))
                                .event(TextComponent("${ChatColor.RED}点击删除这个地标").hoverEvent)
                                .create()
                        val buttonSet =
                            ComponentBuilder("${ChatColor.GREEN}${ChatColor.UNDERLINE}设为当前位置${ChatColor.RESET}")
                                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp set $name force"))
                                .event(TextComponent("点击输入指令").hoverEvent)
                                .create()
                        val textName = when(val world = Bukkit.getWorld(location.world)!!.name) {
                            "world" -> "${ChatColor.GREEN}${ChatColor.BOLD}$name"
                            "world_nether" -> "${ChatColor.DARK_RED}${ChatColor.BOLD}$name"
                            "world_the_end" -> "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}$name"
                            else -> "${ChatColor.BLUE}${ChatColor.BOLD}$name${ChatColor.GRAY} in " +
                                    "${ChatColor.RESET}${ChatColor.BOLD}$world"
                        }
                        sender.sendMessage("${ChatColor.GOLD}=======================================")
                        sender.sendMessage("${ChatColor.GOLD} 地标 $textName${ChatColor.GOLD} 的详细信息")
                        sender.sendMessage("${ChatColor.GOLD}  - 坐标: ${ChatColor.RESET}" +
                                "${ChatColor.UNDERLINE}$x${ChatColor.RESET}, " +
                                "${ChatColor.UNDERLINE}$y${ChatColor.RESET}, " +
                                "${ChatColor.UNDERLINE}$z")
                        sender.spigot().sendMessage(*ComponentBuilder("${ChatColor.GOLD}  - 描述: ")
                            .append(ComponentBuilder("${ChatColor.RESET}${entry.description}")
                                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp set-des $name "))
                                .event(TextComponent("点击修改").hoverEvent)
                                .create())
                            .create())
                        sender.spigot().sendMessage(*ComponentBuilder(" ")
                            .append(buttonShare).append(" ").append(buttonDelete).append(" ").append(buttonSet)
                            .create())
                        sender.sendMessage("${ChatColor.GOLD}=======================================")
                    }
                }
                else sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }

            // warp set-des <name> <description>
            "set-des" -> {
                if (args.size > 2) {
                    val entry = warps[sender.uniqueId]!![args[1]]
                    if (entry == null)
                        sender.sendMessage("${ChatColor.RED}地标 ${ChatColor.GOLD}${args[1]}${ChatColor.RED} 不存在")
                    else {
                        entry.description = args[2]
                        sender.sendMessage("成功修改地标 ${ChatColor.GOLD}${args[1]}${ChatColor.RESET} 的描述")
                    }
                }
                else sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }

            // warp share <name>
            "share" -> {
                if (args.size > 1) {
                    val entry = warps[sender.uniqueId]!![args[1]]
                    if (entry == null) {
                        sender.sendMessage("${ChatColor.RED}地标 ${ChatColor.GOLD}${args[1]}${ChatColor.RED} 不存在")
                    } else {
                        val location = entry.location
                        val name = entry.name
                        val x = floor(location.x)
                        val y = floor(location.y)
                        val z = floor(location.z)
                        val textName = when(val world = Bukkit.getWorld(location.world)!!.name) {
                            "world" -> "${ChatColor.GREEN}${ChatColor.BOLD}$name"
                            "world_nether" -> "${ChatColor.DARK_RED}${ChatColor.BOLD}$name"
                            "world_the_end" -> "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}$name"
                            else -> "${ChatColor.BLUE}${ChatColor.BOLD}$name${ChatColor.GRAY} in " +
                                    "${ChatColor.RESET}${ChatColor.BOLD}$world"
                        }
                        val addButton =
                            ComponentBuilder("${ChatColor.GREEN}${ChatColor.UNDERLINE}添加该位置${ChatColor.RESET}")
                                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                    "/warp set $name ${location.x} ${location.y} ${location.z}"))
                                .event(TextComponent("点击输入指令").hoverEvent)
                                .create()
                        sender.sendMessage("${ChatColor.GOLD}=======================================")
                        sender.sendMessage("${ChatColor.AQUA} ${sender.displayName}" +
                                "${ChatColor.GOLD} 分享了地标 $textName${ChatColor.GOLD}")
                        sender.sendMessage("${ChatColor.GOLD}  - 坐标: ${ChatColor.RESET}" +
                                "${ChatColor.UNDERLINE}$x${ChatColor.RESET}, " +
                                "${ChatColor.UNDERLINE}$y${ChatColor.RESET}, " +
                                "${ChatColor.UNDERLINE}$z")
                        sender.sendMessage("${ChatColor.GOLD}  - 描述: ${ChatColor.RESET}${entry.description}")
                        sender.spigot().sendMessage(*ComponentBuilder(" ").append(addButton).create())
                        sender.sendMessage("${ChatColor.GOLD}=======================================")
                    }
                }
                else sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }

            // warp find
            "find" -> {
                if (args.size > 1 && args[1].isNotEmpty()) {
                    val tmpWarps = warps[sender.uniqueId]!!
                    //筛选关键词
                    val keyWord = args[1]
                    val tmp: ArrayList<LocationEntry> = ArrayList()
                    tmpWarps.forEach { (name, loc) ->
                        if (name.contains(keyWord, ignoreCase = true)) tmp += loc
                    }
                    if (tmp.isEmpty()) {
                        sender.sendMessage("${ChatColor.RED}未找到任何地标")
                    }
                    else {
                        sender.sendMessage("${ChatColor.GOLD} 在 ${tmp.size} 个地标中找到了 " +
                                "${ChatColor.RED}${ChatColor.BOLD}$keyWord${ChatColor.RESET}${ChatColor.GOLD} :")
                        tmp.forEach { loc ->
                            getWarpInfo(loc)?.let { sender.spigot().sendMessage(*it) }
                        }
                    }
                }
            }
            else -> {
                sender.spigot().sendMessage(*HomeEntity.instance.commandHelp)
            }
        }
        return true
    }

    private fun checkName(name: String, sender: CommandSender): Boolean {
        if(name in ops) {
            sender.sendMessage("${ChatColor.GOLD}$name${ChatColor.RED} 不可以做为地标的名称, 更多内容请查看帮助")
            return false
        }
        return true
    }

    private fun getWarpInfo(entry: LocationEntry): Array<out BaseComponent>? {
        val location = entry.location
        val name = entry.name
        val x = floor(location.x)
        val y = floor(location.y)
        val z = floor(location.z)
        val buttonShare =
            ComponentBuilder("${ChatColor.AQUA}[分享]")
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp share $name"))
                .event(TextComponent("点击将地标信息发送给所有人").hoverEvent)
                .create()
        val buttonDelete =
            ComponentBuilder("${ChatColor.RED}[删除]")
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp rm $name"))
                .event(TextComponent("${ChatColor.RED}点击删除这个地标").hoverEvent)
                .create()
        val buttonSet =
            ComponentBuilder("${ChatColor.GREEN}[设为当前]")
                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp set $name force"))
                .event(TextComponent("点击输入指令").hoverEvent)
                .create()
        val textName = when(val world = Bukkit.getWorld(location.world)!!.name) {
            "world" -> "${ChatColor.GREEN}${ChatColor.BOLD}$name"
            "world_nether" -> "${ChatColor.DARK_RED}${ChatColor.BOLD}$name"
            "world_the_end" -> "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}$name"
            else -> "${ChatColor.BLUE}${ChatColor.BOLD}$name${ChatColor.GRAY} in " +
                    "${ChatColor.RESET}${ChatColor.BOLD}$world"
        }
        return ComponentBuilder(textName)
                .event(TextComponent(entry.description).hoverEvent)
                .append("${ChatColor.GRAY} at ${ChatColor.RESET}" +
                    "${ChatColor.UNDERLINE}$x${ChatColor.RESET}, " +
                    "${ChatColor.UNDERLINE}$y${ChatColor.RESET}, " +
                    "${ChatColor.UNDERLINE}$z${ChatColor.RESET} ")
            .append(buttonShare).append(" ").append(buttonDelete).append(" ").append(buttonSet)
            .create()
    }

    private fun getWarps(): MutableMap<UUID, LinkedHashMap<String, LocationEntry>> {
        val initWarps: MutableMap<UUID, List<LocationEntry>> = ModuledPlayerDataManager.getAllTyped("warp")
        val warps = hashMapOf<UUID, LinkedHashMap<String, LocationEntry>>()
        initWarps.forEach { (uuid, entries) ->
            val tmpLinkedMap = linkedMapOf<String, LocationEntry>()
            entries.forEach { tmpLinkedMap[it.name] = it }
            warps[uuid] = tmpLinkedMap
        }
        return warps
    }

    override fun save() {
        val tmpMap: MutableMap<UUID, List<LocationEntry>> = hashMapOf()
        warps.forEach { (uuid, entries) -> tmpMap[uuid] = entries.values.toList() }
        ModuledPlayerDataManager.setAllTyped("warp", tmpMap)
    }

    override fun config(uuid: UUID): MutableMap<String, LocationEntry>? = warps[uuid]
}