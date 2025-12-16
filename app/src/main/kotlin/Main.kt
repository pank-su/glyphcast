package su.pank.exhelp.app


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel



fun main() {
    return application {
        val windowState = rememberWindowState(size = DpSize(300.dp, 50.dp))


        Window(
            onCloseRequest = ::exitApplication,
            undecorated = true,
            transparent = true,
            state = windowState,
            resizable = false,
            alwaysOnTop = true,
            focusable = false
        ) {

            val viewModel: MainViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle(MainState.Loading)



            WindowDraggableArea {


                when (state) {
                    MainState.Loading -> LoadingScreen()

                    is MainState.ShowContent -> {
                        val content = (state as MainState.ShowContent)
                        LaunchedEffect(state) {
                            if (content.visible) {
                                windowState.size = DpSize(300.dp, 50.dp)
                            } else {
                                windowState.size = DpSize.Zero
                            }
                        }
                        ShowContent(content.text)
                    }

                    is MainState.WaitText -> CodeScreen((state as MainState.WaitText).roomCode)
                }
            }

        }
    }
}


@Composable
fun LoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun CodeScreen(code: String) {
    val clipboardManager = LocalClipboardManager.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(code, style = MaterialTheme.typography.headlineLarge)
            IconButton({
                clipboardManager.setText(buildAnnotatedString { append(code) })
            }) {
                Icon(Icons.Default.ContentCopy, null)
            }
        }
    }
}

@Composable
fun ShowContent(text: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Text(text, Modifier.align(Alignment.Center))
    }
}