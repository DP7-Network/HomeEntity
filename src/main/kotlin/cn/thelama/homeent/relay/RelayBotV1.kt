package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.BaseResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.io.IOException

@Deprecated("V1 Bot has Deprecated and will remove soon! Please use new Bot", ReplaceWith("cn.thelama.homeent.relay.TelegramBotV2"), DeprecationLevel.WARNING)
class RelayBotV1(private val groupId: Long, private val token: String): Relay {
    override val version = 1
    private val botInstance: TelegramBot = TelegramBot.Builder(token).okHttpClient(HomeEntity.instance.httpClient).build()

    init {
        MainScope().launch {
            initBot()
        }
    }

    override suspend fun initBot() {
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

    override fun say(from: String, msg: String) {
        say("$from: $msg")
    }

    override fun say(msg: String) {
        synchronized(botInstance) {
            botInstance.execute(SendMessage(groupId, msg), callback({ _, _ -> }, { _, err ->
                Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                    if(err != null) {
                        Bukkit.getLogger().warning("Failed to send packet! ${err::class.java.name}: ${err.message}")
                    }
                })
            }))
        }
    }

    override fun restartBot(operator: CommandSender?) {
    }

    override suspend fun shutdown() {
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