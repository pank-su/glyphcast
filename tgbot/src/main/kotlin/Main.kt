import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.getMyCommands
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
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
                    """.trimMargin()
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
            supabaseRepository.connectToRoom(code, it.chat.id.chatId.long.toString())

            reply(
                it,
                "Подключение прошло успешно, теперь вы можете отправлять текст на экран. Для отключения введите команду /disconnect или /connect для подключения по другому коду"
            )

        }

        onCommand("disconnect"){
            val channel = supabaseRepository.getUserRoom(it.chat.id.chatId.long.toString())
            if (channel == null){
                reply(it, "Вы и так не подключены. Для подключения используйте команду /connect")
                return@onCommand
            }
            supabaseRepository.disconnectUser(it.chat.id.chatId.long.toString())
            reply(it, "Вы отключены от комнаты.")
        }


        onText {

            val channel = supabaseRepository.getUserRoom(it.chat.id.chatId.long.toString())
            if (channel == null){
                reply(it, "Вы не подключены. Для подключения используйте команду /connect")
                return@onText
            }
            channel.sendMessage(it.content.text)
        }

    }.join()
}