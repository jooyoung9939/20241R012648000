package api

import closet.ClosetApiService
import mannequin.MannequinApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import signin.SigninApiService

object RetrofitClient {
    private const val BASE_URL = "http://43.203.15.197:3000/"
    private const val CLIP_DROP_BASE_URL = "https://clipdrop-api.co/"

    val clip_drop_instance: ClosetApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(CLIP_DROP_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        retrofit.create(ClosetApiService::class.java)
    }

    val closet_instance: ClosetApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ClosetApiService::class.java)
    }

    val signin_instance: SigninApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SigninApiService::class.java)
    }

    val mannequin_instance: MannequinApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MannequinApiService::class.java)
    }
}