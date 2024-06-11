package com.example.lookatme

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.deckor_teamc_front.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FetchDataViewModel(application: Application) : AndroidViewModel(application) {
    private val _userResponse = MutableLiveData<UserResponse>()
    val userResponse: LiveData<UserResponse> get() = _userResponse

    private val _duplicateResponse = MutableLiveData<DuplicateResponse>()
    val duplicateResponse: LiveData<DuplicateResponse> get() = _duplicateResponse

    private val _backgroundRemovalResponse = MutableLiveData<ByteArray>()
    val backgroundRemovalResponse: LiveData<ByteArray> get() = _backgroundRemovalResponse

    private val _uploadResponse = MutableLiveData<ResponseBody>()
    val uploadResponse: LiveData<ResponseBody> get() = _uploadResponse

    private val service = RetrofitClient.instance
    private val clipDropService = ClipDropRetrofitClient.instance

    fun createUser(request: UserRequest) {
        service.createUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    _userResponse.value = response.body()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun loginUser(request: UserRequest) {
        service.loginUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    _userResponse.value = response.body()
                    response.body()?.let {
                        it.AccessToken?.let { token -> TokenManager.saveAccessToken(getApplication(), token) }
                        it.RefreshToken?.let { token -> TokenManager.saveRefreshToken(getApplication(), token) }
                    }
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    fun checkIdAvailability(loginId: String) {
        service.checkIdAvailability(loginId).enqueue(object : Callback<DuplicateResponse> {
            override fun onResponse(call: Call<DuplicateResponse>, response: Response<DuplicateResponse>) {
                if (response.isSuccessful) {
                    _duplicateResponse.value = response.body()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DuplicateResponse>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
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
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }

    private fun refreshAccessToken(onSuccess: () -> Unit) {
        val refreshToken = TokenManager.getRefreshToken(getApplication())
        refreshToken?.let {
            service.refreshAccessToken("Bearer $refreshToken").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { userResponse ->
                            userResponse.AccessToken?.let { token -> TokenManager.saveAccessToken(getApplication(), token) }
                            onSuccess()
                        }
                    } else {
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        }
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
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Access Token is null")
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
            Log.e("FetchDataViewModel", "Access Token is null")
            data.value = emptyList()
        }

        return data
    }

}
