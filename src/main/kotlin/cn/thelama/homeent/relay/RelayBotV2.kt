package cn.thelama.homeent.relay

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.mylovelycat.FeedResult
import cn.thelama.homeent.mylovelycat.MyLovelyCat
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithParams
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import kotlin.math.abs
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
class RelayBotV2(private val groupId: Long, private val token: String): Relay {
    override val version = 2
    private lateinit var bot: TelegramBot
    var scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            initBot()
        }
    }

    @OptIn(PreviewFeature::class, dev.inmo.tgbotapi.utils.RiskFeature::class)
    override suspend fun initBot() {
        bot = telegramBot(token) {
            engine {
                proxy = HomeEntity.instance.globalNetworkProxy
            }
        }

        bot.sendTextMessage(ChatId(groupId), "[+] HomeEntity Relay Bot")

        bot.buildBehaviour(scope, defaultExceptionsHandler = {
            if(it !is CancellationException) {
                it.printStackTrace()
                println("Relay V2 Got a exception! Please check it above then report to developers.")
            }
        }) {
            onCommand("onlines") { msg ->
                val onlines = Bukkit.getOnlinePlayers()
                when(onlines.size) {
                    0 -> {
                        sendMessage(msg.chat, "????????????LSP???????????????, ????????????????????????")
                    }

                    1 -> {
                        sendMessage(msg.chat, "?????????${onlines.size}???LSP??????????????????????????????: \n ${onlines.joinToString(separator = " ") { it.name }}")
                    }

                    else -> {
                        sendMessage(msg.chat, "?????????${onlines.size}???LSP?????????????????????: \n ${onlines.joinToString(separator = " ") { it.name }}")

                    }
                }
            }

            onCommand("debug", requireOnlyCommandInMessage = false) {
                for((k, v) in it.parseCommandsWithParams()) {
                    reply(it, "$k - $v")
                }
            }

            onCommand("cat", requireOnlyCommandInMessage = false) {
                if(it.asFromUserMessage()?.user?.username?.username == "abslama" && it.parseCommandsWithParams()["cat"]?.contains("reset") == true) {
                    Bukkit.broadcastMessage("${ChatColor.GOLD}?????????????????????????????????! ?????????????????????${String.format("%.2f", MyLovelyCat.weight())}kg")
                    sendMessage(it.chat, "?????????????????????????????????! ?????????????????????${String.format("%.2f", MyLovelyCat.weight())}kg")
                } else {
                    sendMessage(it.chat, "?????????????????????: ${String.format("%.2f", MyLovelyCat.weight())}kg")
                }
            }

            onCommand("feed") {
                val msg = it.asFromUserMessage() ?: return@onCommand
                val add = Random.nextDouble(-10.0, 10.0)
                when(MyLovelyCat.feed(add)) {
                    FeedResult.INCREASE -> {
                        sendMessage(it.chat, "${ChatColor.GREEN}??????!???????????????${ChatColor.GOLD}${String.format("%.2f", add)}${ChatColor.GREEN}kg, ???????????????: ${ChatColor.GOLD}${String.format("%.2f", MyLovelyCat.weight())}${ChatColor.GREEN}kg")
                    }

                    FeedResult.DECREASE -> {
                        sendMessage(it.chat, "${ChatColor.RED}????????????????????????????????????${ChatColor.GOLD}${String.format("%.2f", abs(add))}${ChatColor.RED}kg, ???????????????: ${ChatColor.GOLD}${String.format("%.2f", MyLovelyCat.weight())}${ChatColor.RED}kg")
                    }

                    FeedResult.DEATH -> {
                        Bukkit.broadcastMessage("${ChatColor.RED}???!!! ${ChatColor.STRIKETHROUGH}${msg.user.firstName} ${msg.user.lastName}??????????????????!")
                    }
                }
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
                        } else {
                            println("Ignored non-target chat message from: ${it.chat.id.chatId}")
                        }
                    }

                    else -> {
                        println("Ignored ${it.content::class.simpleName} type of chat message from: ${it.chat.id.chatId}")
                    }
                }
            }
        }.join()
    }

    override fun restartBot(operator: CommandSender?) {
        GlobalScope.launch {
            scope.cancel()
            scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
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
        bot.sendTextMessage(ChatId(groupId), "[-] HomeEntity Relay Bot")
        scope.cancel()
    }

    override fun say(from: String, msg: String) {
        say("$from: $msg")
    }

    override fun say(msg: String) {
        scope.launch {
            bot.sendTextMessage(ChatId(groupId), msg)
        }
    }
}