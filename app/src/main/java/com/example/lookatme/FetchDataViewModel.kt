package com.example.lookatme

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.deckor_teamc_front.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FetchDataViewModel : ViewModel() {
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

    fun uploadClothes(category: String, imagePath: String, type: String, memo: String) {
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        service.uploadClothes(category, body, type, memo).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    _uploadResponse.value = response.body()
                } else {
                    Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FetchDataViewModel", "Failure: ${t.message}")
            }
        })
    }
}
