package API

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ClipDropRetrofitClient {
    private const val BASE_URL = "https://clipdrop-api.co/"

    val instance: ClipDropApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        retrofit.create(ClipDropApiService::class.java)
    }
}
