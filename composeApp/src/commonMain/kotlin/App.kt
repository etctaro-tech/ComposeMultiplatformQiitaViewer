import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() {
    MaterialTheme {
        Navigator(ListScreen)
    }
}


object ListScreen : Screen {

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var resultList by rememberSaveable { mutableStateOf(listOf<QiitaArticle>()) }

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(imageFileName()), contentDescription = "")
                        Text("Sample List")
                    }},
                    actions = {
                        IconButton(onClick = {
                            resultList = listOf()
                        }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "clear")
                        }
                    }
                )
            },
            floatingActionButton = { FloatingActionButton(onClick = {
                MainScope().launch {
                    val result = ExampleApi().getArticles()
                    resultList = result
                }
            }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
            } }
        ) {
            LazyColumn {
                items(resultList) {
                    Column(modifier = Modifier.clickable {
                        navigator.push(DetailScreen(it.url))
                    }) {
                        Text(it.title, style = MaterialTheme.typography.body1)
                        Text(it.url, style = MaterialTheme.typography.body2, modifier = Modifier.padding(start = 5.dp))
                    }
                    Divider()
                }
            }
        }
    }

}


data class DetailScreen(val url: String = "") : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state = rememberWebViewState(url.ifEmpty { "https://google.co.jp" })

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Sample Detail") }
                    , navigationIcon = {
                        IconButton(onClick = {navigator.pop()}){
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                        }
                    }
                )
            },
        ){
            WebView(state)
        }
    }
}