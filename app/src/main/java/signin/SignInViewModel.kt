package signin

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

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.signin_instance

    private val _userResponse = MutableLiveData<UserResponse>()
    val userResponse: LiveData<UserResponse> get() = _userResponse

    private val _duplicateResponse = MutableLiveData<DuplicateResponse>()
    val duplicateResponse: LiveData<DuplicateResponse> get() = _duplicateResponse

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
                        it.AccessToken?.let { token ->
                            TokenManager.saveAccessToken(
                                getApplication(),
                                token
                            )
                        }
                        it.RefreshToken?.let { token ->
                            TokenManager.saveRefreshToken(
                                getApplication(),
                                token
                            )
                        }
                        it.nickname?.let { nickname ->
                            TokenManager.saveNickname(
                                getApplication(),
                                nickname
                            )
                        }
                        it.uuid?.let { uuid ->
                            TokenManager.saveUuid(
                                getApplication(),
                                uuid
                            )
                        }
                        TokenManager.setLoggedIn(getApplication(), true)
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
}