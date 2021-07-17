package cn.thelama.homeent.secure

import cn.thelama.homeent.HomeEntity
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.PacketPlayInChat
import net.minecraft.network.protocol.game.PacketPlayInKeepAlive
import net.minecraft.network.protocol.game.PacketPlayOutChat
import net.minecraft.network.protocol.game.PacketPlayOutKeepAlive
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer

class PlayerLoginNetworkIntercepter(private val player: CraftPlayer) : ChannelDuplexHandler() {
    private val packets = mutableListOf<Packet<*>>()

    override fun channelRead(ctx: ChannelHandlerContext?, obj: Any?) {
        if(obj is PacketPlayInChat) {
            val str = obj.b()
            if(str.startsWith(".") || str.startsWith("/")) {
                val sl = str.split(" ")
                if(sl.size >= 2) {
                    if(sl[1].length < 4 || sl[1].length > 21) {
                        player.sendMessage("${ChatColor.RED}密码长度必须为4到21位")
                    }
                    when(sl[0].toLowerCase()) {
                        ".l", ".i", ".login" -> {
                            if(SecureHandler.checkCredentials(player.uniqueId, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}登陆成功！欢迎回家 :)")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/Lama3L9R/HomeEntity")
                                SecureHandler.setLoginState(player.uniqueId, true)
                                HomeEntity.instance.logger.info("send packet for ${player.name} total: ${packets.size}")
                                sendCachedPackets()
                                SecureHandler.removeLimit(player)
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }

                        ".r", ".reg", ".register" -> {
                            if(SecureHandler.register(player.uniqueId, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}注册成功, 欢迎来到.DP7 996 Days")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/Lama3L9R/HomeEntity")
                                SecureHandler.setLoginState(player.uniqueId, true)
                                sendCachedPackets()
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }
                    }
                } else {
                    player.sendMessage("${ChatColor.RED}参数不正确")
                    player.sendMessage("${ChatColor.GREEN}正确的登录方法：'.l <密码>'")
                    player.sendMessage("${ChatColor.GREEN}正确使用方法：'.r <密码>'")
                }
            }
            player.sendMessage("${ChatColor.RED}请先登录!")
        } else if(obj is PacketPlayInKeepAlive) {
            super.channelRead(ctx, obj)
        }

        if(SecureHandler.getLoginState(player.uniqueId)) {
            SecureHandler.removeLimit(player)
            super.channelRead(ctx, obj)
        }
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if(SecureHandler.getLoginState(player.uniqueId)) {
            SecureHandler.removeLimit(player)
            super.write(ctx, msg, promise)
            return
        }

        if(msg is PacketPlayOutKeepAlive || msg is PacketPlayOutChat) {
            super.write(ctx, msg, promise)
        } else if(msg is Packet<*>) {
            packets.add(msg)
        }
    }

    private fun sendCachedPackets() {
        val con = player.handle.b
        packets.forEach {
            println(it::class.java.name)
        }
        packets.forEach {
            HomeEntity.instance.logger.info("send packet to${player.name} name: ${it::class.java.simpleName}")
            con.a.k.writeAndFlush(it)
        }
        SecureHandler.removeLimit(player)
    }
}