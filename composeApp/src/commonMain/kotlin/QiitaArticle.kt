import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QiitaArticle (
    @SerialName("id")
    val id: String,

    @SerialName("title")
    val title: String,

    @SerialName("url")
    val url: String
)
