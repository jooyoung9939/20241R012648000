package scrap

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import api.RetrofitClient
import api.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import signin.UserResponse

class ScrapViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.scrap_instance

    val lookBooksLiveData = MutableLiveData<List<LookBook>>()
    val topsLiveData = MutableLiveData<List<SavedClothesResponse>>()
    val pantsLiveData = MutableLiveData<List<SavedClothesResponse>>()
    val shoesLiveData = MutableLiveData<List<SavedClothesResponse>>()
    val accessoriesLiveData = MutableLiveData<List<SavedClothesResponse>>()

    private fun refreshAccessToken(onSuccess: () -> Unit) {
        val refreshToken = TokenManager.getRefreshToken(getApplication())
        if (refreshToken != null) {
            service.refreshAccessToken("Bearer $refreshToken").enqueue(object :
                Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { userResponse ->
                            userResponse.AccessToken?.let { token ->
                                TokenManager.saveAccessToken(
                                    getApplication(),
                                    token
                                )
                            }
                            onSuccess()
                        }
                    } else {
                        Log.e("ScrapViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("ScrapViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ScrapViewModel", "Refresh Token is null")
        }
    }

    fun getSavedLookBook() {
        val accessToken = TokenManager.getAccessToken(getApplication())
        if (accessToken != null) {
            service.getSavedLookBook("Bearer $accessToken").enqueue(object :
                Callback<SavedLookBookResponse> {
                override fun onResponse(call: Call<SavedLookBookResponse>, response: Response<SavedLookBookResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            lookBooksLiveData.postValue(it.clippedLookBookCollection)
                        }
                    } else {
                        Log.e("ScrapViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<SavedLookBookResponse>, t: Throwable) {
                    Log.e("ScrapViewModel", "Failure: ${t.message}")
                }
            })
        }
    }

    fun getSavedClothes(category: String): LiveData<List<SavedClothesResponse>> {
        val liveData = when (category) {
            "tops" -> topsLiveData
            "pants" -> pantsLiveData
            "shoes" -> shoesLiveData
            "accessories" -> accessoriesLiveData
            else -> throw IllegalArgumentException("Unknown category")
        }

        val accessToken = TokenManager.getAccessToken(getApplication())
        if (accessToken != null) {
            service.getSavedClothes("Bearer $accessToken", category).enqueue(object :
                Callback<List<SavedClothesResponse>> {
                override fun onResponse(call: Call<List<SavedClothesResponse>>, response: Response<List<SavedClothesResponse>>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            liveData.postValue(it)
                        }
                    } else {
                        Log.e("ScrapViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<SavedClothesResponse>>, t: Throwable) {
                    Log.e("ScrapViewModel", "Failure: ${t.message}")
                }
            })
        }
        return liveData
    }
}
