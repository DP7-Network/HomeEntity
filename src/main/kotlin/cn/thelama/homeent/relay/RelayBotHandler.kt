package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object RelayBotHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if (command.name == "relay") {
            if (args.isNotEmpty()) when (args[0]) {
                "restart" -> {
                    val bot: RelayBot = HomeEntity.instance.botInstance;
                    bot.restartBot();
                }
            //TODO Other functions
            }
            else sender.sendMessage("${ChatColor.RED}This command need an argument!")

        }
        return true
    }

}
