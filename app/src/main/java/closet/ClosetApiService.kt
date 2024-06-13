package closet

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

interface ClosetApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>
    @Multipart
    @POST("remove-background/v1")
    fun removeBackground(
        @Header("x-api-key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST("clothes/upload/{category}")
    fun uploadClothes(
        @Header("Authorization") token: String,
        @Path("category") category: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String,
        @Part("memo") memo: String
    ): Call<ResponseBody>

    @GET("clothes/{category}")
    fun getClothes(
        @Header("Authorization") token: String,
        @Path("category") category: String
    ): Call<List<ClothesItem>>

    @GET("clothes/{category}/{id}")
    fun getClothesDetail(
        @Header("Authorization") token: String,
        @Path("category") category: String,
        @Path("id") id: Int
    ): Call<ClothesDetail>
}

data class ClothesItem(
    val id: Int,
    val url: String
)

data class ClothesDetail(
    val id: Int,
    val category: String,
    val url: String,
    val type: String,
    val memo: String
)
