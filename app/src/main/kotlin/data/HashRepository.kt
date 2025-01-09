package data

import com.russhwolf.settings.Settings
import org.kotlincrypto.hash.sha2.SHA512
import java.util.Date

object HashRepository {
    private val settings = Settings()

    @OptIn(ExperimentalStdlibApi::class)
    val hash: String
        get() = settings.getStringOrNull("hash") ?: run {
            val hash: String = SHA512().digest(Date().time.toString().toByteArray()).toHexString()
            settings.putString("hash", hash)
            hash
        }
}