package profile

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

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.profile_instance

    private val _lookBookData = MutableLiveData<LookBookResponse>()
    val lookBookData: LiveData<LookBookResponse> get() = _lookBookData

    private val _mannequinData = MutableLiveData<MannequinResponse>()
    val mannequinData: LiveData<MannequinResponse> get() = _mannequinData

    private val _myProfileData = MutableLiveData<MyProfileResponse>()
    val myProfileData: LiveData<MyProfileResponse> get() = _myProfileData

    private val _otherProfileData = MutableLiveData<OtherProfileResponse>()
    val otherProfileData: LiveData<OtherProfileResponse> get() = _otherProfileData

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
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ProfileViewModel", "Refresh Token is null")
        }
    }

    fun getProfileLookBook(userUUID: String, take: Int, cursor: Int, keyword: String) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getProfileLookBook("Bearer $accessToken", userUUID, take, cursor, keyword).enqueue(object : Callback<LookBookResponse> {
                override fun onResponse(call: Call<LookBookResponse>, response: Response<LookBookResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _lookBookData.value = it
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getProfileLookBook(userUUID, take, cursor, keyword)
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LookBookResponse>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ProfileViewModel", "Access Token is null")
        }
    }

    fun getProfileMannequin(take: Int, cursor: Int) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getProfileMannequin("Bearer $accessToken", take, cursor).enqueue(object : Callback<MannequinResponse> {
                override fun onResponse(call: Call<MannequinResponse>, response: Response<MannequinResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _mannequinData.value = it
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getProfileMannequin(take, cursor)
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MannequinResponse>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ProfileViewModel", "Access Token is null")
        }
    }

    fun getMyProfile() {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getMyProfile("Bearer $accessToken").enqueue(object : Callback<MyProfileResponse> {
                override fun onResponse(call: Call<MyProfileResponse>, response: Response<MyProfileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _myProfileData.value = it
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getMyProfile()
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MyProfileResponse>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ProfileViewModel", "Access Token is null")
        }
    }

    fun getOtherProfile(userUUID: String) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getOtherProfile("Bearer $accessToken", userUUID).enqueue(object : Callback<OtherProfileResponse> {
                override fun onResponse(call: Call<OtherProfileResponse>, response: Response<OtherProfileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _otherProfileData.value = it
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            getOtherProfile(userUUID)
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<OtherProfileResponse>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("ProfileViewModel", "Access Token is null")
        }
    }

    fun followUser(userUUID: String, callback: (Boolean) -> Unit) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.followUser("Bearer $accessToken", userUUID).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("ProfileViewModel", "Successfully followed the user.")
                        callback(true)
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            followUser(userUUID, callback)
                        }
                    } else {
                        Log.e("ProfileViewModel", "Error response: ${response.errorBody()?.string()}")
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("ProfileViewModel", "Failure: ${t.message}")
                    callback(false)
                }
            })
        } else {
            Log.e("ProfileViewModel", "Access Token is null")
            callback(false)
        }
    }

}
