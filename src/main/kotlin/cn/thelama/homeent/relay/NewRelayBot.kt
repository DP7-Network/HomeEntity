package cn.thelama.homeent.relay

import dev.inmo.tgbotapi.extensions.api.telegramBot

class NewRelayBot(private val groupId: Long, private val token: String) {
    private val bot = telegramBot(token) {
    }

    fun sendMessage(str: String) {
    }

    fun restartBot() {
    }
}
