package cn.thelama.homeent.warp

import cn.thelama.homeent.HomeEntity
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.stream.Collectors

object WarpCompleter : TabCompleter {
    private val ops = listOf("add", "del", "list", "lookup")
    override fun onTabComplete(sender: CommandSender, cmd: Command, lable: String, args: Array<out String>): List<String>? {
        if(cmd.name == "warp") {
            if(args.isNotEmpty() && (args.size == 1 || args[0] == "del" || args[0] == "lookup")) {
                if(args.size > 1) {
                    return HomeEntity.instance.warps.keys.stream().filter { it.startsWith(args[1]) }.collect(Collectors.toList()) + ops
                }
                return HomeEntity.instance.warps.keys.stream().filter { it.startsWith(args[0]) }.collect(Collectors.toList()) + ops
            }
        }
        return null
    }
}