package su.pank.exhelp.app


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
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
import androidx.compose.ui.platform.LocalUriHandler
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
        // TODO: change size on connect
        val windowState = rememberWindowState(size = DpSize(350.dp, 300.dp))


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
                MaterialTheme {
                    when (state) {
                        MainState.Loading -> LoadingScreen()

                        is MainState.ShowSupabaseContent -> {
                            val content = (state as MainState.ShowSupabaseContent)
                            LaunchedEffect(state) {
                                if (content.visible) {
                                    windowState.size = DpSize(200.dp, 120.dp)
                                } else {
                                    windowState.size = DpSize.Zero
                                }
                            }
                            ShowContent(content.text)
                        }
                        
                        is MainState.ShowGeminiContent -> {
                            val content = (state as MainState.ShowGeminiContent)
                            LaunchedEffect(state) {
                                windowState.size = DpSize(300.dp, 120.dp)
                            }
                            ShowContent(content.text) {
                                viewModel.stopGeminiSession()
                            }
                        }
                        
                        is MainState.WaitUser -> WaitScreen((state as MainState.WaitUser), viewModel)
                    }
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

// TODO: add viewmodel
@Composable
fun WaitScreen(waitState: MainState.WaitUser, viewModel: MainViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Подключить друга:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(waitState.roomCode, style = MaterialTheme.typography.headlineLarge)
                IconButton({
                    clipboardManager.setText(buildAnnotatedString { append(waitState.roomCode) })
                }) {
                    Icon(Icons.Default.ContentCopy, null)
                }
            }
            Text(
                "@glyphcastbot", 
                color = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.clickable{
                    uriHandler.openUri("https://t.me/glyphcastbot")
                }.align(Alignment.CenterHorizontally)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text("API ключ Gemini Flash:")
            OutlinedTextField(
                value = viewModel.geminiApiKey,
                onValueChange = { viewModel.updateGeminiApiKey(it) }, 
                placeholder = {
                    Text("Введите API ключ")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(Modifier.height(8.dp))
            
            if (waitState.hasGeminiKey) {
                Button(
                    onClick = { viewModel.startGeminiSession() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Запустить сессию с Gemini")
                }
            } else {
                Text(
                    "Введите API ключ для запуска сессии с Gemini",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ShowContent(text: String, onStop: (() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f))) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            onStop?.let {
                Button(onClick = it) {
                    Text("Остановить")
                }
            }
        }
    }
}