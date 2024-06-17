package profile

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import signin.UserResponse

interface ProfileApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>

    @GET("lookbook/profile/{userUUID}")
        fun getProfileLookBook(
        @Header("Authorization") refreshToken: String,
        @Path("userUUID") userUUID: String,
        @Query("take") take: Int,
        @Query("cursor") cursor: Int,
        @Query("keyword") keyword: String
    ): Call<LookBookResponse>

    @GET("lookbook/mannequin-lookbook")
    fun getProfileMannequin(
        @Header("Authorization") refreshToken: String,
        @Query("take") take: Int,
        @Query("cursor") cursor: Int,
    ): Call<MannequinResponse>
}
data class LookBookCollection(
    val lookbookId: Int,
    val tops: Top,
    val accessories: Accessories,
    val pant: Pant,
    val shoe: Shoe
)

data class Top(
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

data class LookBookResponse(
    val lookBookCollection: List<LookBookCollection>,
    val cursorPaginationMetaData: CursorPaginationMetaData
)

data class MannequinLookBookCollection(
    val id: Int,
    val url: String
)

data class MannequinResponse(
    val mannequinLookBookCollection: List<MannequinLookBookCollection>,
    val cursorPaginationMetaData: CursorPaginationMetaData
)
