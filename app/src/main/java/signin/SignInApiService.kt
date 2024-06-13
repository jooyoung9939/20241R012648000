package signin

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SigninApiService {
    @POST("auth/create-user")
    fun createUser(
        @Body request: UserRequest
    ): Call<UserResponse>

    @POST("auth/login")
    fun loginUser(
        @Body request: UserRequest
    ): Call<UserResponse>

    @GET("auth/duplicate/{loginId}")
    fun checkIdAvailability(
        @Path("loginId") loginId: String
    ): Call<DuplicateResponse>
}

data class UserRequest(
    val loginId: String,
    val password: String,
    val phoneNumber: String? = null,
    val nickname: String? = null
)
data class UserResponse(
    val statusCode: Int,
    val message: String,
    val nickname: String?,
    val AccessToken: String? = null,
    val RefreshToken: String? = null
)

data class DuplicateResponse(
    val message: String
)