package su.pank.exhelp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Часть операций требуют другой ключ
val supabaseKey = System.getenv("supabase_key")
    ?: "sb_publishable_GpnTq01Zlfba5-ATIwRNqA_tPdY4TFE"

object SupabaseRepository {

    suspend fun getOrCreateRoom(hash: String): String {
        return supabaseClient.postgrest.rpc("get_or_create_room", CreateRoomArgs(hash)).data.replace("\"", "")
    }

    suspend fun listenRoom(roomCode: String): Flow<RoomData> {
        val channel = supabaseClient.realtime.channel(roomCode)
        channel.subscribe(true)
        return channel.broadcastFlow<Message>("message").combine(channel.broadcastFlow<Visibility>("visible")){ mes: Message, vis: Visibility ->
            RoomData(mes.text, vis.visible)
        }
    }

    suspend fun checkRoomExists(roomCode: String): Boolean {
        val data = supabaseClient.postgrest.rpc("room_code_exists", CheckCodeArgs(roomCode))
        return data.data == "true"
    }


    suspend fun connectToRoom(roomCode: String, userId: String): RealtimeChannel {
        supabaseClient.postgrest.rpc("connect_user_to_room", ConnectUserToRoomArgs(roomCode, userId))
        return supabaseClient.channel(roomCode)
    }

    suspend fun getUserRoom(userId: String): RealtimeChannel? {
        val roomCode = supabaseClient.postgrest.rpc("get_room_code_by_user", GetRoomByUserIdArgs(userId))
            .runCatching { decodeAs<String>() }.getOrNull()
        return if (roomCode != null) supabaseClient.channel(roomCode) else null

    }

    suspend fun disconnectUser(userId: String){
        supabaseClient.postgrest.rpc("disconnect_user_from_room", GetRoomByUserIdArgs(userId))
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

    @Serializable
    private data class GetRoomByUserIdArgs(@SerialName("user_id_") val userId: String)

    @Serializable
    data class RoomData(val message: String, val visible: Boolean)


    private val supabaseClient = createSupabaseClient(
        "https://xxpvxircmvxuaxrhlzbr.supabase.co",
        supabaseKey
    ) {
        install(Postgrest)
        install(Realtime)

        defaultLogLevel = LogLevel.DEBUG
    }


}

@Serializable
data class Message(val text: String)

@Serializable
data class Visibility(val visible: Boolean)

suspend fun RealtimeChannel.sendMessage(text: String) {
    broadcast("message", Message(text))
}

suspend fun RealtimeChannel.setVisibility(value: Boolean){
    broadcast("visible", Visibility(value))
}