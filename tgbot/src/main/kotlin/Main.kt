package su.pank.exhelp.tgbot

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onTextedContent
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.updates.hasNoCommands
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.content.TextMessage
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.flow.first
import su.pank.exhelp.*

val TOKEN = System.getenv("token")

val waitedTextUsers = mutableSetOf<String>()


object Messages{
    val notConnectedMessage = "Вы не подключены. Для подключения используйте команду /connect"
}


suspend fun main() {
    val bot = telegramBot(TOKEN)

    val supabaseRepository = SupabaseRepository

    bot.buildBehaviourWithLongPolling {
        println(getMe())

        onCommand("start") {
            reply(
                it,
                """Привет это бот для отображения текста на экране. 
                    |Для того чтобы отобразить опр. текст на твоём экране, необходимо ввести код из приложения. 
                    """.trimMargin()
            )
        }

        onCommand("connect") {
            waitedTextUsers.add(it.chat.id.chatId.long.toString())
            val code = waitText(
                SendTextMessage(
                    it.chat.id,
                    "Пришли пожалуйста код. Для отмены ввода используй команду /cancel"
                )
            ).first { content ->
                if (content.text == "/cancel") {
                    reply(it, "Ввод кода отменён")
                    return@first true
                }
                if (!supabaseRepository.checkRoomExists(content.text)) {
                    reply(it, "Код комнаты неверный")
                    return@first false
                } else {
                    return@first true
                }
            }.text
            waitedTextUsers.remove(it.chat.id.chatId.long.toString())

            if (code == "/cancel"){
                return@onCommand
            }
            val channel = supabaseRepository.connectToRoom(code, it.chat.id.chatId.long.toString())
            kotlin.runCatching {
                channel.setVisibility(true)
            }.onFailure {
                it.printStackTrace()
            }


            reply(
                it,
                "Подключение прошло успешно, теперь вы можете отправлять текст на экран. Для отключения введите команду /disconnect или /connect для подключения по другому коду"
            )

        }

        onCommand("disconnect"){
            checkConnectionAndGetChannel(supabaseRepository, it) ?:  return@onCommand

            supabaseRepository.disconnectUser(it.chat.id.chatId.long.toString())
            reply(it, "Вы отключены от комнаты.")
        }

        onCommand("hide"){
            val channel = checkConnectionAndGetChannel(supabaseRepository, it) ?:  return@onCommand

            channel.setVisibility(false)
        }

        onCommand("show"){
            val channel = checkConnectionAndGetChannel(supabaseRepository, it) ?:  return@onCommand

            channel.setVisibility(true)
        }


        onText(initialFilter = {it.hasNoCommands() && !waitedTextUsers.contains(it.chat.id.chatId.long.toString())}) {
            val channel = checkConnectionAndGetChannel(supabaseRepository, it) ?:  return@onText
            channel.sendMessage(it.content.text)
            channel.setVisibility(true)
        }

    }.join()
}

private suspend fun BehaviourContext.checkConnectionAndGetChannel(
    supabaseRepository: SupabaseRepository,
    message: TextMessage
): RealtimeChannel? {
    val channel = supabaseRepository.getUserRoom(message.chat.id.chatId.long.toString())
    if (channel == null) {
        reply(message, Messages.notConnectedMessage)
    }
    return channel
}