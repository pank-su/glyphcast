import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.takeWhile

val TOKEN = System.getenv("token")



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
                    |Приложение, можно скачать здесь""".trimMargin()
            )
        }

        onCommand("connect") {
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
            if (code == "/cancel"){
                return@onCommand
            }
            val channel = supabaseRepository.connectToRoom(code, it.chat.id.chatId.long.toString())

            waitText(SendTextMessage(
                it.chat.id,
                "Подключение прошло успешно, теперь вы можете отправлять текст на экран"
            )).collect{
                channel.sendMessage(it.text)
            }

        }

        onText {

        }

    }.join()
}