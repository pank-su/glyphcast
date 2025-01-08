import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object SupabaseRepository {

    suspend fun getOrCreateRoom(hash: String): String {
        return supabaseClient.postgrest.rpc("get_or_create_room", CreateRoomArgs(hash)).data.replace("\"", "")
    }

    suspend fun listenRoom(roomCode: String): Flow<String> {
        val channel = supabaseClient.realtime.channel(roomCode)
        channel.subscribe(true)
        return channel.broadcastFlow<Message>("message").map { it.text }
    }

    suspend fun checkRoomExists(roomCode: String): Boolean {
        val data = supabaseClient.postgrest.rpc("room_code_exists", CheckCodeArgs(roomCode))
        return data.data == "true"
    }


    suspend fun connectToRoom(roomCode: String, userId: String): RealtimeChannel {
        supabaseClient.postgrest.rpc("connect_user_to_room", ConnectUserToRoomArgs(roomCode, userId))
        return supabaseClient.channel(roomCode).apply { subscribe(true) }
    }

    @Serializable
    private data class CreateRoomArgs(@SerialName("user_hash_input") val hash: String)

    @Serializable
    private data class ConnectUserToRoomArgs(
        @SerialName("room_code_") val roomCode: String,
        @SerialName("user_id_") val userId: String
    )

    @Serializable
    private data class CheckCodeArgs(
        @SerialName("room_code_") val roomCode: String,
    )


    private val supabaseClient = createSupabaseClient(
        "https://gshxctseczzjqtulpnpu.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdzaHhjdHNlY3p6anF0dWxwbnB1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzYzMzE5MzcsImV4cCI6MjA1MTkwNzkzN30.qs1Z_JKc8qAeWb30dgPsh5H2K1WioSPjNioWuOSpABg"
    ) {
        install(Postgrest)
        install(Realtime)

        defaultLogLevel = LogLevel.DEBUG
    }


}

@Serializable
data class Message(val text: String)

suspend fun RealtimeChannel.sendMessage(text: String) {
    broadcast("message", Message(text))
}