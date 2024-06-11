package API

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ClipDropApiService {
    @Multipart
    @POST("remove-background/v1")
    fun removeBackground(
        @Header("x-api-key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>
}
