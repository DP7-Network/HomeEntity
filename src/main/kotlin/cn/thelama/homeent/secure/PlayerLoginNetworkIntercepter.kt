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
            if(str.startsWith(".")) {
                val sl = str.split(" ")
                if(sl.size >= 2) {
                    if(sl[1].length < 4 || sl[1].length > 21) {
                        player.sendMessage("${ChatColor.RED}密码长度必须为4到21位")
                    }
                    when(sl[0].toLowerCase()) {
                        ".l", ".i", ".login" -> {
                            if(AuthHandler.checkCredentials(player.uniqueId, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}登陆成功！欢迎回家 :)")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/Lama3L9R/HomeEntity")
                                AuthHandler.setLoginState(player.uniqueId, true)
                                sendCachedPackets()
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }

                        ".r", ".reg", ".register" -> {
                            if(sl.size < 2) {
                                player.sendMessage("${ChatColor.RED}格式错误! .r <密码> <重复密码>")
                                return
                            }

                            if(sl[1] != sl[2]) {
                                player.sendMessage("${ChatColor.RED}重复密码不正确!")
                                return
                            }

                            if(AuthHandler.register(player.uniqueId, sl[1])) {
                                player.sendMessage("${ChatColor.GREEN}注册成功, 欢迎来到 ${HomeEntity.instance.config.getString("main.serverName")}")
                                player.sendMessage("${ChatColor.GREEN}有关指令帮助请访问: https://github.com/DP7-Network/HomeEntity")
                                AuthHandler.setLoginState(player.uniqueId, true)
                                sendCachedPackets()
                            } else {
                                player.sendMessage("${ChatColor.RED}密码错误! 您注册了吗?")
                            }
                            return
                        }

                        else -> {
                            player.sendMessage("${ChatColor.RED}参数不正确")
                            player.sendMessage("${ChatColor.GREEN}正确的登录方法：'.l <密码>'")
                            player.sendMessage("${ChatColor.GREEN}正确的注册方法：'.r <密码> <重复密码>'")
                            player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}指令无需 '/' 请直接发送 '.r' / '.l' 到聊天区")
                        }
                    }
                } else {
                    player.sendMessage("${ChatColor.RED}参数不正确")
                    player.sendMessage("${ChatColor.GREEN}正确的登录方法：'.l <密码>'")
                    player.sendMessage("${ChatColor.GREEN}正确的注册方法：'.r <密码> <重复密码>'")
                    player.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}指令无需 '/' 请直接发送 '.r' / '.l' 到聊天区")
                }
            }
            player.sendMessage("${ChatColor.RED}请先登录!")
        } else if(obj is PacketPlayInKeepAlive) {
            super.channelRead(ctx, obj)
        }

        if(AuthHandler.getLoginState(player.uniqueId)) {
            AuthHandler.removeLimit(player)
            super.channelRead(ctx, obj)
        }
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if(AuthHandler.getLoginState(player.uniqueId)) {
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
        packets.forEach {
         // craftPlayer.nmsEntityPlayer.playerConnection.networkManager.channel
            player.handle.b.a.k.writeAndFlush(it)
        }
        AuthHandler.removeLimit(player)
    }
}