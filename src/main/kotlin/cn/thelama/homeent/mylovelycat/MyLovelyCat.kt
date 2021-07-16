package cn.thelama.homeent.mylovelycat

import cn.thelama.homeent.HomeEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.math.abs
import kotlin.random.Random

/**
 * lama的猫猫
 */
object MyLovelyCat : CommandExecutor {
    private var weight: Double = 0.0

    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<out String>): Boolean {
        when(command.name) {
            "cat" -> {
                if(sender.name == "Lama3L9R" && "reset" in args) {
                    reset(Math.random() * 10 + 7)
                    Bukkit.broadcastMessage("${ChatColor.GOLD}猫猫的主人将猫猫重置啦! 现在猫猫的重量${weight}kg")
                    HomeEntity.instance.botInstance.say("${ChatColor.GOLD}猫猫的主人将猫猫重置啦! 现在猫猫的重量${weight}kg")
                } else {
                    sender.sendMessage("${ChatColor.GOLD}当前猫猫的重量: ${weight}kg")
                }
            }

            "feed" -> {
                val add = Random.nextDouble(-10.0, 10.0)
                when(feed(add)) {
                    FeedResult.INCREASE -> {
                        Bukkit.broadcastMessage("${ChatColor.GREEN}好耶! 猫猫被${sender.name}又喂胖了${ChatColor.GOLD}${String.format("%.2f", add)}${ChatColor.GREEN}kg, 现在猫猫有: ${ChatColor.GOLD}${String.format("%.2f", weight)}${ChatColor.GREEN}kg")
                        HomeEntity.instance.botInstance.say("好耶! 猫猫被${sender.name}又喂胖了${String.format("%.2f", add)}kg, 现在猫猫有: ${String.format("%.2f", weight)}kg")
                    }

                    FeedResult.DECREASE -> {
                        Bukkit.broadcastMessage("${ChatColor.RED}猫猫因为${sender.name}而没吃舒服导致瘦了${ChatColor.GOLD}${String.format("%.2f", abs(add))}${ChatColor.RED}kg, 现在猫猫有: ${ChatColor.GOLD}${String.format("%.2f", weight)}${ChatColor.RED}kg")
                        HomeEntity.instance.botInstance.say("猫猫因为${sender.name}而没吃舒服导致瘦了${String.format("%.2f", abs(add))}kg, 现在猫猫有: ${String.format("%.2f", weight)}kg")
                    }

                    FeedResult.DEATH -> {
                        Bukkit.broadcastMessage("${ChatColor.RED}坏!!! ${ChatColor.STRIKETHROUGH}${sender.name}吧猫猫杀死了!")
                        HomeEntity.instance.botInstance.say("坏!!! **${sender.name}**吧猫猫杀死了!")
                    }
                }
            }
        }
        return true
    }

    fun feed(weight: Double): FeedResult {
        synchronized(weight) {
            val event = CatWeightChangedEvent(weight, this.weight - weight, this.weight + weight)
            Bukkit.getServer().pluginManager.callEvent(event)
            if(event.isCancelled) {
                this.weight -= weight
            } else {
                this.weight += event.increaseWeight
            }

            return if(weight < 0) {
                val deathEvent = CatDeathEvent(event.increaseWeight, this.weight - event.increaseWeight, this.weight, Math.random() * 10 + 7)
                Bukkit.getServer().pluginManager.callEvent(event)
                this.weight = deathEvent.next
                FeedResult.DEATH
            } else {
                return if(event.increaseWeight > 0) {
                    FeedResult.INCREASE
                } else {
                    FeedResult.DECREASE
                }
            }
        }
    }

    fun reset(weight: Double) {
        Bukkit.getServer().pluginManager.callEvent(CatDeathEvent(0.0, 0.0, 0.0, weight, reset = true))
        this.weight = weight
    }

    fun init(pluginInstance: HomeEntity, weight: Double) {
        this.weight = weight
    }

    fun weight() = this.weight
}