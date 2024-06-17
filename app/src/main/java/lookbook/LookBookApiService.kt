package lookbook

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import signin.UserResponse

interface LookBookApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>

    @GET("clothes/{category}")
    fun getClothes(
        @Header("Authorization") token: String,
        @Path("category") category: String
    ): Call<List<LookBookClothesItem>>

    @GET("mannequin/me")
    fun getMannequin(
        @Header("Authorization") token: String,
    ): Call<LookBookMannequin>

    @Multipart
    @POST("lookbook")
    fun uploadLookBooks(
        @Header("Authorization") token: String,
        @Part("topIds") topIds: List<Int>,
        @Part("pantId") pantId: Int,
        @Part("shoeId") shoeId: Int,
        @Part("accessoryIds") accessoryIds: List<Int>,
        @Part("show") show: Boolean,
        @Part("title") title: String,
        @Part("type") type: List<String>,
        @Part("memo") memo: String,
        @Part file: MultipartBody.Part,
    ): Call<ResponseBody>
}

data class LookBookClothesItem(
    val id: Int,
    val url: String
)

data class LookBookMannequin(
    val sex: Int,
    val hair: Int,
    val skinColor: Int,
    val height: Int,
    val body: Int,
    val arm: Int,
    val leg: Int
)

