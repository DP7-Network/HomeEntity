package cn.thelama.homeent.tpa

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

object TPAcceptHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "tpaccept" && sender is Player && args.size == 2) {
            when(args[0]) {
                "go" -> {
                    TPManager.acceptGo(sender, UUID.fromString(args[1]))
                }

                "here" -> {
                    TPManager.acceptHere(sender, UUID.fromString(args[1]))
                }
            }
        }
        return true
    }
}