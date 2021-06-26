package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

@OptIn(DelicateCoroutinesApi::class)
class RelayBotV2(private val groupId: Long, private val token: String): Relay {
    override val version = 2
    private lateinit var bot: TelegramBot
    private var task = GlobalScope.async {
        initBot()
    }

    @OptIn(PreviewFeature::class, dev.inmo.tgbotapi.utils.RiskFeature::class)
    override suspend fun initBot() {
        bot = telegramBot(token) {
            engine {
                proxy = HomeEntity.instance.globalNetworkProxy
            }
        }

        bot.buildBehaviour(GlobalScope) {
            onCommand("list") { msg ->
                val onlines = Bukkit.getOnlinePlayers()
                if(onlines.size == 1) {
                    sendMessage(msg.chat, "目前有${onlines.size}个LSP在服务器中和自己击剑: \n ${onlines.joinToString(separator = " ") { it.name }}")
                } else {
                    sendMessage(msg.chat, "目前有${onlines.size}个LSP在服务器中击剑: \n ${onlines.joinToString(separator = " ") { it.name }}")
                }
            }

            onCommand("debug") {
                reply(it, it.content.text)
            }

            onContentMessage {
                when(val content = it.content) {
                    is TextContent -> {
                        if(groupId == it.chat.id.chatId) {
                            val usr = it.asFromUserMessage()?.user
                            val name = if(usr?.firstName == null && usr?.lastName == null) {
                                usr?.username?.username
                            } else {
                                "${usr.firstName} ${usr.lastName}"
                            }
                            Bukkit.broadcastMessage("${ChatColor.AQUA}[${ChatColor.GREEN}RELAY${ChatColor.AQUA}] ${ChatColor.YELLOW}$name${ChatColor.RESET}: ${content.text}")
                        }
                    }
                }
            }
        }.join()
    }

    override fun restartBot(operator: CommandSender?) {
        GlobalScope.launch {
            task.cancelAndJoin()
            task = GlobalScope.async {
                initBot()
            }

            if(operator != null) {
                Bukkit.getScheduler().runTask(HomeEntity.instance, Runnable {
                    operator.sendMessage("${ChatColor.GREEN}RelayBot V2 restarted successfully!")
                })
            }
        }
    }

    override suspend fun shutdown() {
        task.cancelAndJoin()
    }

    override fun say(from: String, msg: String) {
        say("$from: $msg")
    }

    override fun say(msg: String) {
        GlobalScope.launch {
            bot.sendTextMessage(ChatId(groupId), msg)
        }
    }
}