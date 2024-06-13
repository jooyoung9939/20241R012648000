package mannequin

import closet.ClothesItem
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import signin.UserResponse

interface MannequinApiService {
    @GET("auth/new-access-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): Call<UserResponse>
    @FormUrlEncoded
    @PUT("mannequin/me")
    fun editMannequin(
        @Header("Authorization") token: String,
        @Field("sex") sex: Int,
        @Field("hair") hair: Int,
        @Field("skinColor") skinColor: Int,
        @Field("height") height: Int,
        @Field("body") body: Int,
        @Field("arm") arm: Int,
        @Field("leg") leg: Int
    ): Call<Mannequin>

    @GET("mannequin/me")
    fun getMannequin(
        @Header("Authorization") token: String,
    ): Call<Mannequin>
}

data class Mannequin(
    val sex: Int,
    val hair: Int,
    val skinColor: Int,
    val height: Int,
    val body: Int,
    val arm: Int,
    val leg: Int
)