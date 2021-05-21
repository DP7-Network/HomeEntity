package cn.thelama.homeent.exit

import net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange
import net.minecraft.server.v1_16_R3.PacketPlayOutExplosion
import net.minecraft.server.v1_16_R3.Vec3D
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object ExitHandler : CommandExecutor {
    val texts = listOf("See you next time", "Dont forget you will here forever", "Goodbye")

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "exit") {
            if(sender is Player) {
                if(args.isNotEmpty()) {
                    if(args[0] == "crash") {
                        (sender as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutExplosion(
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Double.MAX_VALUE,
                            Float.MAX_VALUE,
                            mutableListOf(),
                            Vec3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)))
                    }
                }
                sender.kickPlayer(texts.random())
            }
        }
        return true
    }
}