import com.google.gson.annotations.SerializedName

data class Response(

    @SerializedName("data") val data: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("url") val url: String
)