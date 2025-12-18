package su.pank.exhelp.app.data

import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object HashRepository {
    private val settings = Settings()

    @OptIn(ExperimentalStdlibApi::class, ExperimentalUuidApi::class)
    val hash: String
        get() = settings.getStringOrNull("hash") ?: run {
            val hash: String = Uuid.random().toHexString().substring(0..5)
            settings.putString("hash", hash)
            hash
        }
}