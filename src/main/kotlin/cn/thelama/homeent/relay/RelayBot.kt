package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import okhttp3.OkHttpClient
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

class RelayBot(private val groupId: Long, private val token: String) {
    private val botInstance = TelegramBot.Builder(token).okHttpClient(OkHttpClient.Builder().proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("192.168.1.102", 1089))).build()).build()

    init {
        botInstance.setUpdatesListener({
            it.forEach { update ->
                if(update.message() == null) {
                    Bukkit.getLogger().info("Ignoring trash message from null")

                    return@forEach
                }
                val chat = update.message().chat()
                if(chat.id() != groupId) {
                    Bukkit.getLogger().info("Ignoring trash message from id: ${chat.id()}")
                    return@forEach
                }
                val from = update.message().from()
                val name = if(from.firstName() == null && from.lastName() == null) {
                    Bukkit.getLogger().info("A nameless user is speaking...")
                    from.username()
                } else {
                    val lastName = from.lastName()
                    "${from.firstName()}${if (lastName == null) "" else " $lastName"}"
                }

                if(update.message().text() != null) {
                    Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                        Bukkit.broadcastMessage("${ChatColor.AQUA}[${ChatColor.GREEN}RELAY${ChatColor.AQUA}] ${ChatColor.YELLOW}$name${ChatColor.RESET}: ${update.message().text()}")
                    })
                } else {
                    Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                        Bukkit.broadcastMessage("${ChatColor.AQUA}[${ChatColor.GREEN}RELAY${ChatColor.AQUA}] ${ChatColor.YELLOW}$name${ChatColor.RESET}: ${ChatColor.GRAY}[Not Supported]")
                    })
                }
            }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }) {
            Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                Bukkit.getLogger().warning("Failed to poll updates! ${it::class.java.name}: ${it.message}")
            })
        }
    }

    fun sendMessage(str: String) {
        synchronized(botInstance) {
            botInstance.execute(SendMessage(groupId, str), callback({ _, _ -> }, { _, err ->
                Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                    if(err != null) {
                        Bukkit.getLogger().warning("Failed to send packet! ${err::class.java.name}: ${err.message}")
                    }
                })
            }))
        }
    }

    private fun <T : BaseRequest<T, R>, R : BaseResponse> callback(res: (BaseRequest<T, R>, BaseResponse) -> Unit, fail: (BaseRequest<*, *>, IOException?) -> Unit): Callback<T, R> {
        return object: Callback<T, R> {
            override fun onResponse(request: T, response: R) {
                res(request, response)
            }

            override fun onFailure(request: T, e: IOException?) {
                fail(request, e)
            }
        }
    }
}