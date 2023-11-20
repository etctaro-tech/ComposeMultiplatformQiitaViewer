
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExampleApi {
    private val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    companion object {
        private const val QIITA_URI = "https://qiita.com/api/v2/items?page=1&per_page=20"
    }

    suspend fun getArticles(): List<QiitaArticle> {
        return client.get(QIITA_URI).body()
    }
}
