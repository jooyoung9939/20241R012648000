package scrap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import signin.UserResponse

interface ScrapApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>

    @GET("lookbook/clip/all")
    fun getSavedLookBook(
        @Header("Authorization") accessToken: String
    ): Call<SavedLookBookResponse>

    @GET("clothes/clips/all/{category}")
    fun getSavedClothes(
        @Header("Authorization") accessToken: String,
        @Path("category") category: String
    ): Call<List<SavedClothesResponse>>
}

data class SavedLookBookResponse(
    val clippedLookBookCollection: List<LookBook>
)

data class LookBook(
    val lookbookId: Int,
    val tops: Tops,
    val accessories: Accessories,
    val pant: Pant,
    val shoe: Shoe
)

data class Tops(
    val urls: List<String>
)

data class Accessories(
    val urls: List<String>
)

data class Pant(
    val url: String
)

data class Shoe(
    val url: String
)

data class SavedClothesResponse(
    val id: Int,
    val url: String
)
