package su.pank.exhelp.app.data

import com.russhwolf.settings.Settings

object GeminiKeyRepository {
    private val settings = Settings()
    
    var apiKey: String
        get() = settings.getStringOrNull("gemini_api_key") ?: ""
        set(value) {
            settings.putString("gemini_api_key", value)
        }
    
    val hasApiKey: Boolean
        get() = apiKey.isNotEmpty()
}
