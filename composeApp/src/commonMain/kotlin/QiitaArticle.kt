import io.realm.kotlin.types.RealmObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class QiitaArticle (
    @SerialName("id")
    var id: String = "",

    @SerialName("title")
    var title: String = "",

    @SerialName("url")
    var url: String = ""
) : RealmObject {
    constructor() : this("")
}