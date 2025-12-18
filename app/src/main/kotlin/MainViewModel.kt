package su.pank.exhelp.app


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import su.pank.exhelp.app.data.HashRepository
import kotlinx.coroutines.flow.*
import su.pank.exhelp.SupabaseRepository

sealed interface MainState{
    object Loading: MainState
    data class WaitUser(val roomCode: String): MainState
    data class ShowContent(val text: String, val visible: Boolean): MainState
}


class MainViewModel(
    private val hashRepository: HashRepository = HashRepository,
    private val supabaseRepository: SupabaseRepository = SupabaseRepository
) : ViewModel() {

    var isMovable: Boolean by mutableStateOf(false)

    val state = flow<MainState> {
        val hash = hashRepository.hash
        val code = supabaseRepository.getOrCreateRoom(hash)
        emit(MainState.WaitUser(code))

        supabaseRepository.listenRoom(code).collect{
            emit(MainState.ShowContent(it.message, visible = it.visible))
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

}