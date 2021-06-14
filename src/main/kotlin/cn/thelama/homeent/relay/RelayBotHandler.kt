package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

object RelayBotHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if (command.name == "relay") {
            if (args.isNotEmpty() && (sender is ConsoleCommandSender || (sender is Player && sender.uniqueId in HomeEntity.instance.maintainers))) when (args[0]) {
                "restart" -> {
                    val bot: RelayBot = HomeEntity.instance.botInstance
                    bot.restartBot()
                }
            }
        }
        return true
    }

}
