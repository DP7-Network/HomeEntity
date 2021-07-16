package cn.thelama.homeent.warp

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object HomeHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}抱歉, 该指令专属于玩家")
            return true
        }

        when (command.name) {
            "home" -> {
                val location = WarpHandlerV2.getHomeLocation(sender)
                if (location == null) {
                    sender.sendMessage("${ChatColor.YELLOW}未设置家")
                    return true
                }
                sender.teleport(location)
            }

            "sethome" -> {
                if (WarpHandlerV2.setHomeLocation(sender, args.isNotEmpty() && (args[0] == "force")))
                    sender.sendMessage("${ChatColor.YELLOW}成功设置家")
                else {
                    val commandTip = ComponentBuilder(
                        "${ChatColor.GOLD}/sethome force${ChatColor.RESET}")
                        .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sethome force"))
                        .event(TextComponent("点击输入").hoverEvent)
                        .create()
                    sender.spigot().sendMessage(*ComponentBuilder("${ChatColor.YELLOW}家位置已存在, 请使用 ")
                            .append(commandTip).append("${ChatColor.YELLOW} 强制覆盖已有位置").create())
                }
            }
        }

        return true
    }
}