import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.tools.javac.Main
import data.HashRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface MainState{
    object Loading: MainState
    data class WaitText(val roomCode: String): MainState
    data class ShowContent(val text: String): MainState
}


class MainViewModel(
    private val hashRepository: HashRepository = HashRepository,
    private val supabaseRepository: SupabaseRepository = SupabaseRepository
) : ViewModel() {

    val state = flow<MainState> {
        val hash = hashRepository.hash
        val code = supabaseRepository.getOrCreateRoom(hash)
        emit(MainState.WaitText(code))

        supabaseRepository.listenRoom(code).collect{
            emit(MainState.ShowContent(it))
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

}