package cn.thelama.homeent.tpa

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object TPDenyHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "tpdeny" && sender is Player && args.size == 2) {
            when(args[0]) {
                "go" -> {
                    TPManager.denyGo(sender, UUID.fromString(args[1]))
                }

                "here" -> {
                    TPManager.denyHere(sender, UUID.fromString(args[1]))
                }
            }
        }
        return true
    }
}