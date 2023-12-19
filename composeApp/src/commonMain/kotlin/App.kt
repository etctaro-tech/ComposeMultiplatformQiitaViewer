import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
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

    @OptIn(ExperimentalResourceApi::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var resultList by rememberSaveable { mutableStateOf(listOf<QiitaArticle>()) }
        var loading by remember { mutableStateOf(false) }
        val hostState = remember { SnackbarHostState() }
        var apiResultList by rememberSaveable { mutableStateOf(listOf<QiitaArticle>()) }

        val configuration = RealmConfiguration.create(schema = setOf(QiitaArticle::class))
        val realm = Realm.open(configuration)

        var filtered by rememberSaveable { mutableStateOf(false)}
        var favoriteList by rememberSaveable { mutableStateOf(
            realm.query<QiitaArticle>().find().map {
                QiitaArticle(it.id, it.title, it.url)
            }
        ) }


        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(imageFileName()), contentDescription = "")
                        Text("Sample List")
                    }},
                    actions = {
                        FavoriteToggle(isFavorite = filtered,
                            onToggleChanged = {
                                filtered = !filtered
                                favoriteList = realm.query<QiitaArticle>().find().map {
                                    QiitaArticle(it.id, it.title, it.url)
                                }
                                resultList = if (filtered) {
                                    favoriteList
                                } else {
                                    apiResultList
                                }
                        })

                        IconButton(onClick = {
                            apiResultList = listOf()
                            resultList = listOf()
                        }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "clear")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState) },
            floatingActionButton = { FloatingActionButton(onClick = {
                loading = true
                MainScope().launch {
                    try {
                        val result = ExampleApi().getArticles()
                        apiResultList = result
                        if (!filtered) resultList = apiResultList
                    } catch (e: Exception) {
                        // なんかメッセージを出す形にしよう。
                        hostState.showSnackbar("取得に失敗しました")
                    } finally {
                        loading = false
                    }
                }
            }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
            } }
        ) {
            LazyColumn {
                items(resultList) { article ->
                    var isFavorite by remember { mutableStateOf(realm.query<QiitaArticle>(QiitaArticle::class, "url = $0", article.url).find().size > 0) }

                    ListItem(trailing = {
                        if (!filtered) {
                            FavoriteToggle(isFavorite) {
                                isFavorite = it
                                if (isFavorite) {
                                    MainScope().launch(Dispatchers.IO) {
                                        realm.write {
                                            copyToRealm(
                                                QiitaArticle(
                                                    id = article.id,
                                                    title = article.title,
                                                    url = article.url
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    MainScope().launch(Dispatchers.IO) {
                                        realm.write {
                                            val query =
                                                query<QiitaArticle>(
                                                    QiitaArticle::class,
                                                    "id = $0",
                                                    article.id
                                                )
                                            delete(query)
                                        }
                                    }
                                }
                            }
                        }
                    },
                        modifier = Modifier.clickable {
                            navigator.push(DetailScreen(article.url))
                        }
                    ) {
                        Column {
                            Text(article.title, style = MaterialTheme.typography.body1)
                        }
                    }
                    Divider()
                }
            }
            // ローディングを追加
            if (loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    @Composable
    private fun FavoriteToggle(
        isFavorite: Boolean,
        onToggleChanged: (Boolean) -> Unit
    ) {
        IconToggleButton(checked = isFavorite,
            onCheckedChange = onToggleChanged
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "favorite"
            )
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
            val loadState = state.loadingState
            Column {
                // ローディングを追加
                if (loadState is LoadingState.Loading) {
                    LinearProgressIndicator(progress = loadState.progress, modifier = Modifier.fillMaxWidth())
                }
                WebView(state)
            }
        }
    }
}