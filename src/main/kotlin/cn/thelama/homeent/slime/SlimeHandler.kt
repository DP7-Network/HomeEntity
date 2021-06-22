package cn.thelama.homeent.slime

import cn.thelama.homeent.HomeEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.abs

object SlimeHandler : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        if(command.name == "slime") {
            if(sender is Player) {
                if(args.isNotEmpty()) {
                    kotlin.runCatching {
                        val iBound = args[0].toInt()
                        if(abs(iBound) > 10) {
                            sender.sendMessage("${ChatColor.RED}请输入一个${ChatColor.BOLD}合理${ChatColor.RESET}${ChatColor.RED}的数字范围...")
                            return true
                        }
                        Bukkit.getScheduler().runTaskAsynchronously(HomeEntity.instance, Runnable {
                            val offsetRange = -iBound..iBound
                            val baseChunk = sender.world.getChunkAt(sender.location)
                            val baseX = baseChunk.x
                            val baseY = baseChunk.z
                            for(x in offsetRange) {
                                for(z in offsetRange) {
                                    val chunk = sender.world.getChunkAt(baseX + x, baseY + z)
                                    val center = chunk.getBlock(8, 64, 8)
                                    if(chunk.isSlimeChunk) {
                                        sender.sendMessage("已找到史莱姆区块：${ChatColor.BOLD}${ChatColor.YELLOW}${center.x} ~ ${center.z}")
                                    }
                                }
                            }
                        })
                    }.onFailure {
                        sender.sendMessage("${ChatColor.RED}请输入一个合理的${ChatColor.BOLD}数字${ChatColor.RESET}${ChatColor.RED}范围...")
                    }
                } else {
                    sender.sendMessage("${ChatColor.RED}请确定一个搜索范围: ${ChatColor.GOLD}/slime <范围>")
                }
            } else {
                sender.sendMessage("${ChatColor.RED}Only Players can run this command")
            }
        }
        return true
    }
}