package data

import com.russhwolf.settings.Settings
import org.kotlincrypto.hash.sha2.SHA512

object HashRepository {
    private val settings = Settings()

    @OptIn(ExperimentalStdlibApi::class)
    val hash: String
        get() = settings.getStringOrNull("hash") ?: run {
            val hash: String = SHA512().digest().toHexString()
            settings.putString("hash", hash)
            hash
        }
}