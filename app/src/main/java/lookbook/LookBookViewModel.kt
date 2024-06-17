package lookbook

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

class LookBookViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.lookbook_instance

    private val _mannequinData = MutableLiveData<LookBookMannequin>()
    val mannequinData: LiveData<LookBookMannequin> get() = _mannequinData

    private val _uploadResponse = MutableLiveData<ResponseBody>()
    val uploadResponse: LiveData<ResponseBody> get() = _uploadResponse

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
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Refresh Token is null")
        }
    }

    fun getClothes(category: String): LiveData<List<LookBookClothesItem>> {
        val data = MutableLiveData<List<LookBookClothesItem>>()
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getClothes("Bearer $accessToken", category).enqueue(object : Callback<List<LookBookClothesItem>> {
                override fun onResponse(call: Call<List<LookBookClothesItem>>, response: Response<List<LookBookClothesItem>>) {
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

                override fun onFailure(call: Call<List<LookBookClothesItem>>, t: Throwable) {
                    data.value = emptyList()
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Access Token is null")
            data.value = emptyList()
        }

        return data
    }

    fun fetchMannequinData() {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getMannequin("Bearer $accessToken").enqueue(object : Callback<LookBookMannequin> {
                override fun onResponse(call: Call<LookBookMannequin>, response: Response<LookBookMannequin>) {
                    if (response.isSuccessful) {
                        _mannequinData.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            fetchMannequinData()
                        }
                    } else {
                        _mannequinData.value = null
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LookBookMannequin>, t: Throwable) {
                    _mannequinData.value = null
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Access Token is null")
            _mannequinData.value = null
        }
    }

    fun uploadLookBooks(topIds: List<Int>, pantId: Int, shoeId: Int, accessoryIds: List<Int>, show: Boolean, title: String, type: List<String>, memo: String, imagePath: String) {
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.uploadLookBooks("Bearer $accessToken", topIds, pantId, shoeId, accessoryIds, show, title, type, memo, body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _uploadResponse.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            uploadLookBooks(topIds, pantId, shoeId, accessoryIds, show, title, type, memo, imagePath)
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
}
