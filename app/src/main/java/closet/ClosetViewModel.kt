package closet

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import api.RetrofitClient
import api.TokenManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import signin.UserResponse
import java.io.File

class ClosetViewModel(application: Application) : AndroidViewModel(application) {
    private val _backgroundRemovalResponse = MutableLiveData<ByteArray>()
    val backgroundRemovalResponse: LiveData<ByteArray> get() = _backgroundRemovalResponse

    private val _uploadResponse = MutableLiveData<ResponseBody>()
    val uploadResponse: LiveData<ResponseBody> get() = _uploadResponse

    private val service = RetrofitClient.closet_instance
    private val clipDropService = RetrofitClient.clip_drop_instance

    private fun refreshAccessToken(onSuccess: () -> Unit) {
        val refreshToken = TokenManager.getRefreshToken(getApplication())
        if (refreshToken != null) {
            service.refreshAccessToken("Bearer $refreshToken").enqueue(object : Callback<UserResponse> {
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
                        Log.e("ClosetViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("ClosetViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ClosetViewModel", "Refresh Token is null")
        }
    }

    fun removeBackground(apiKey: String, imagePath: String) {
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        val body = MultipartBody.Part.createFormData("image_file", file.name, requestFile)

        clipDropService.removeBackground(apiKey, body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    _backgroundRemovalResponse.value = response.body()?.bytes()
                } else {
                    Log.e("ClosetViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ClosetViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun uploadClothes(category: String, imagePath: String, type: String, memo: String) {
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.uploadClothes("Bearer $accessToken", category, body, type, memo).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _uploadResponse.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            uploadClothes(category, imagePath, type, memo)
                        }
                    } else {
                        Log.e("ClosetViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ClosetViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ClosetViewModel", "Access Token is null")
        }
    }

    fun getClothes(category: String): LiveData<List<ClothesItem>> {
        val data = MutableLiveData<List<ClothesItem>>()
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getClothes("Bearer $accessToken", category).enqueue(object : Callback<List<ClothesItem>> {
                override fun onResponse(call: Call<List<ClothesItem>>, response: Response<List<ClothesItem>>) {
                    if (response.isSuccessful) {
                        data.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getClothes(category).observeForever { refreshedData ->
                                data.value = refreshedData
                            }
                        }
                    } else {
                        data.value = emptyList()
                    }
                }

                override fun onFailure(call: Call<List<ClothesItem>>, t: Throwable) {
                    data.value = emptyList()
                }
            })
        } else {
            Log.e("ClosetViewModel", "Access Token is null")
            data.value = emptyList()
        }

        return data
    }

    fun getClothesDetail(category: String, id: Int): LiveData<ClothesDetail> {
        val data = MutableLiveData<ClothesDetail>()
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getClothesDetail("Bearer $accessToken", category, id).enqueue(object : Callback<ClothesDetail> {
                override fun onResponse(call: Call<ClothesDetail>, response: Response<ClothesDetail>) {
                    if (response.isSuccessful) {
                        data.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getClothesDetail(category, id).observeForever { refreshedData ->
                                data.value = refreshedData
                            }
                        }
                    } else {
                        Log.e("ClosetViewModel", "Error response: ${response.errorBody()?.string()}")
                        data.value = null
                    }
                }

                override fun onFailure(call: Call<ClothesDetail>, t: Throwable) {
                    Log.e("ClosetViewModel", "Failure: ${t.message}")
                    data.value = null
                }
            })
        } else {
            Log.e("ClosetViewModel", "Access Token is null")
            data.value = null
        }

        return data
    }

    fun saveClothes(category: String, id: Int) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.saveClothes("Bearer $accessToken", category, id).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("ClosetViewModel", "Clothes saved successfully")
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            saveClothes(category, id)
                        }
                    } else {
                        Log.e("ClosetViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ClosetViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ClosetViewModel", "Access Token is null")
        }
    }
}
