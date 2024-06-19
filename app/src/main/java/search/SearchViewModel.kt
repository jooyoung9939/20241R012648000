package search

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

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val service = RetrofitClient.search_instance

    private val _searchResults = MutableLiveData<LookBookSearchResponse>()
    val searchResults: LiveData<LookBookSearchResponse> get() = _searchResults

    private var isSearching = false

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
                        Log.e("SearchViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("SearchViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("SearchViewModel", "Refresh Token is null")
        }
    }

    fun searchLookBook(take: Int, cursor: Int, keyword: String) {
        if (isSearching) return

        val accessToken = TokenManager.getAccessToken(getApplication())

        if (accessToken != null) {
            isSearching = true
            service.searchLookBook("Bearer $accessToken", take, cursor, keyword).enqueue(object : Callback<LookBookSearchResponse> {
                override fun onResponse(call: Call<LookBookSearchResponse>, response: Response<LookBookSearchResponse>) {
                    isSearching = false
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _searchResults.value = it
                        }
                    } else if (response.code() == 401) {
                        refreshAccessToken {
                            searchLookBook(take, cursor, keyword)
                        }
                    } else {
                        Log.e("SearchViewModel", "Error response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LookBookSearchResponse>, t: Throwable) {
                    isSearching = false
                    Log.e("SearchViewModel", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("SearchViewModel", "Access Token is null")
        }
    }
}
