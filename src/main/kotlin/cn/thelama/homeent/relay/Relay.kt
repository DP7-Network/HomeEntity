package cn.thelama.homeent.relay

import org.bukkit.command.CommandSender

interface Relay {
    val version: Int
    suspend fun initBot()
    fun say(from: String, msg: String)
    fun say(msg: String)
    fun restartBot(operator: CommandSender? = null)
    suspend fun shutdown()
}