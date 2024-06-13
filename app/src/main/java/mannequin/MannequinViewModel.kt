package mannequin

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

class MannequinViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.mannequin_instance

    private val _editResponse = MutableLiveData<Mannequin>()
    val editResponse: LiveData<Mannequin> get() = _editResponse

    private val _mannequinData = MutableLiveData<Mannequin>()
    val mannequinData: LiveData<Mannequin> get() = _mannequinData

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

    fun editMannequin(sex: Int, hair: Int, skinColor: Int, height: Int, body: Int, arm: Int, leg: Int) {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.editMannequin("Bearer $accessToken", sex, hair, skinColor, height, body, arm, leg).enqueue(object : Callback<Mannequin> {
                override fun onResponse(call: Call<Mannequin>, response: Response<Mannequin>) {
                    if (response.isSuccessful) {
                        _editResponse.value = response.body()
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            editMannequin(sex, hair, skinColor, height, body, arm, leg)
                        }
                    } else {
                        Log.e("FetchDataViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Mannequin>, t: Throwable) {
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Access Token is null")
        }
    }

    fun fetchMannequinData() {
        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            service.getMannequin("Bearer $accessToken").enqueue(object : Callback<Mannequin> {
                override fun onResponse(call: Call<Mannequin>, response: Response<Mannequin>) {
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

                override fun onFailure(call: Call<Mannequin>, t: Throwable) {
                    _mannequinData.value = null
                    Log.e("FetchDataViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("FetchDataViewModel", "Access Token is null")
            _mannequinData.value = null
        }
    }
}
