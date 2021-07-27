package cn.thelama.homeent.warp

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.stream.Collectors

object WarpCompleter : TabCompleter {
    private val ops = WarpHandlerV2.ops
    override fun onTabComplete(sender: CommandSender, cmd: Command, lable: String, args: Array<out String>): List<String>? {
        if(cmd.name == "warp" && sender is Player) {
            if(args.isNotEmpty() && (args[0] == "share" || args[0] == "rm" || args[0] == "detail" || args[0] == "set-des")) {
                if(args.size > 1) {
                    (WarpHandlerV2.config(sender.uniqueId)?.keys?.stream()?.filter { it.startsWith(args[1]) }?.collect(Collectors.toList()) ?: mutableListOf()) + ops
                }
                (WarpHandlerV2.config(sender.uniqueId)?.keys?.stream()?.filter { it.startsWith(args[0]) }?.collect(Collectors.toList()) ?: mutableListOf()) + ops
            } else {
                (WarpHandlerV2.config(sender.uniqueId)?.keys?.stream()?.collect(Collectors.toList()) ?: mutableListOf()) + ops
            }
        }
        return null
    }
}