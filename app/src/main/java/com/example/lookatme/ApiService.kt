package com.example.lookatme

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class UserRequest(val loginId: String, val password: String, val phoneNumber: String? = null, val nickname: String? = null)
data class UserResponse(val statusCode: Int, val message: String, val data: Any)

data class DuplicateResponse(val message: String)

interface ApiService {
    @POST("auth/create-user")
    fun createUser(@Body request: UserRequest): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(@Body request: UserRequest): Call<UserResponse>

    @GET("auth/duplicate/{loginId}")
    fun checkIdAvailability(@Path("loginId") loginId: String): Call<DuplicateResponse>

    @Multipart
    @POST("clothes/upload/{category}")
    fun uploadClothes(
        @Path("category") category: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String,
        @Part("memo") memo: String
    ): Call<ResponseBody>
}
