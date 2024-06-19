package search

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import signin.UserResponse

interface SearchApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>

    @GET("lookbook")
    fun searchLookBook(
        @Header("Authorization") refreshToken: String,
        @Query("take") take: Int,
        @Query("cursor") cursor: Int,
        @Query("keyword") keyword: String
    ): Call<LookBookSearchResponse>
}

data class LookBookSearchResponse(
    val lookBookCollection: List<LookBookCollection>,
    val cursorPaginationMetaData: CursorPaginationMetaData
)

data class LookBookCollection(
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

data class CursorPaginationMetaData(
    val take: Int,
    val cursor: Int,
    val hasNext: Boolean
)