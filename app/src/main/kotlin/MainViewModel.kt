package su.pank.exhelp.app


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import su.pank.exhelp.app.data.HashRepository
import su.pank.exhelp.app.data.GeminiKeyRepository
import su.pank.exhelp.app.data.ScreenshotService
import su.pank.exhelp.app.data.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import su.pank.exhelp.SupabaseRepository

sealed interface MainState{
    object Loading: MainState
    data class WaitUser(val roomCode: String, val hasGeminiKey: Boolean): MainState
    data class ShowSupabaseContent(val text: String, val visible: Boolean): MainState
    data class ShowGeminiContent(val text: String): MainState
}


class MainViewModel(
    private val hashRepository: HashRepository = HashRepository,
    private val supabaseRepository: SupabaseRepository = SupabaseRepository,
    private val geminiKeyRepository: GeminiKeyRepository = GeminiKeyRepository
) : ViewModel() {

    var isMovable: Boolean by mutableStateOf(false)
    
    var geminiApiKey: String by mutableStateOf(geminiKeyRepository.apiKey)
        private set
    
    private var geminiService: GeminiService? = null
    private var geminiJob: Job? = null
    private val _currentState = MutableStateFlow<MainState>(MainState.Loading)
    
    var isGeminiMode: Boolean by mutableStateOf(false)
        private set
    
    init {
        viewModelScope.launch {
            val hash = hashRepository.hash
            val code = supabaseRepository.getOrCreateRoom(hash)
            _currentState.value = MainState.WaitUser(code, geminiKeyRepository.hasApiKey)
            
            // Слушаем Supabase в фоне
            launch {
                supabaseRepository.listenRoom(code).collect {
                    if (!isGeminiMode) {
                        _currentState.value = MainState.ShowSupabaseContent(it.message, visible = it.visible)
                    }
                }
            }
        }
    }
    
    fun updateGeminiApiKey(newKey: String) {
        geminiApiKey = newKey
        geminiKeyRepository.apiKey = newKey
        
        // Обновляем состояние WaitUser если мы на нём
        if (_currentState.value is MainState.WaitUser) {
            val waitUser = _currentState.value as MainState.WaitUser
            _currentState.value = waitUser.copy(hasGeminiKey = newKey.isNotEmpty())
        }
    }
    
    fun startGeminiSession() {
        if (geminiKeyRepository.apiKey.isEmpty()) return
        
        isGeminiMode = true
        geminiService = GeminiService(geminiKeyRepository.apiKey)
        
        // Отменяем предыдущую сессию если есть
        geminiJob?.cancel()
        
        geminiJob = viewModelScope.launch {
            _currentState.value = MainState.ShowGeminiContent("Ожидание первого скриншота...")
            
            ScreenshotService.createScreenshotFlow()
                .collect { screenshotBytes ->
                    geminiService?.let { service ->
                        try {
                            val analysis = service.analyzeScreenshot(screenshotBytes)
                            _currentState.value = MainState.ShowGeminiContent(analysis)
                        } catch (e: Exception) {
                            _currentState.value = MainState.ShowGeminiContent("Ошибка: ${e.message}")
                        }
                    }
                }
        }
    }
    
    fun stopGeminiSession() {
        isGeminiMode = false
        geminiJob?.cancel()
        geminiJob = null
        
        // Возвращаемся в WaitUser состояние
        viewModelScope.launch {
            val hash = hashRepository.hash
            val code = supabaseRepository.getOrCreateRoom(hash)
            _currentState.value = MainState.WaitUser(code, geminiKeyRepository.hasApiKey)
        }
    }

    val state: StateFlow<MainState> = _currentState.asStateFlow()

}