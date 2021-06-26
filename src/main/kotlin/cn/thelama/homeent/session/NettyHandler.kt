package cn.thelama.homeent.session

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

class NettyHandler(private val player: CraftPlayer) : ChannelDuplexHandler() {
    private val packets = mutableListOf<Packet<*>>()

    override fun channelRead(ctx: ChannelHandlerContext?, obj: Any?) {
        if(obj is PacketPlayInChat) {
            if(HomeEntity.instance.isLogin(player)) {
                SessionHandler.removeLimit(player)
                super.channelRead(ctx, obj)
            }
            val str = obj.b()
            if(str.startsWith(".") || str.startsWith("/")) {
                val sl = str.split(" ")
                if(sl.size >= 2) {
                    if(sl[1].length < 4 || sl[1].length > 21) {
                        player.sendMessage("${ChatColor.RED}密码长度必须为4到21位")
                    }
                    when(sl[0].toLowerCase()) {
                        ".l", ".i", ".login", "/.l", "/.i", "/.login", "/l", "/i", "/login" -> {
                            if(HomeEntity.instance.checkCredentials(player, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}登陆成功！欢迎回家 :)")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/Lama3L9R/HomeEntity")
                                synchronized(HomeEntity.instance.unloggedInPlayers) {
                                    HomeEntity.instance.unloggedInPlayers.remove(player.uniqueId)
                                }
                                sendCachedPackets()
                                SessionHandler.removeLimit(player)
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }

                        ".r", ".reg", ".register", "/.r", "/.reg", "/.register", "/r", "/reg", "/register" -> {
                            if(HomeEntity.instance.register(player, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}注册成功, 欢迎来到.DP7 996 Days")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/Lama3L9R/HomeEntity")
                                synchronized(HomeEntity.instance.unloggedInPlayers) {
                                    HomeEntity.instance.unloggedInPlayers.remove(player.uniqueId)
                                }
                                sendCachedPackets()
                                SessionHandler.removeLimit(player)
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }
                    }
                } else {
                    player.sendMessage("${ChatColor.RED}参数不正确")
                    player.sendMessage("${ChatColor.GREEN}正确的登录方法：'/l <密码>'")
                    player.sendMessage("${ChatColor.GREEN}正确使用方法：'/r <密码>'")
                }
            }
            player.sendMessage("${ChatColor.RED}请先登录!")
        } else if(obj is PacketPlayInKeepAlive) {
            super.channelRead(ctx, obj)
        }
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if(msg is PacketPlayOutKeepAlive || msg is PacketPlayOutChat) {
            super.write(ctx, msg, promise)
        } else if(msg is Packet<*>) {
            packets.add(msg)
        }
    }

    private fun sendCachedPackets() {
        val con = player.handle.b
        packets.forEach {
            con.sendPacket(it)
        }
    }
}