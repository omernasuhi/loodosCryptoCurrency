package com.omer.nasuhi.loodoscryptocurrency.ui.main.splash

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.ApiStatusResponse
import com.omer.nasuhi.loodoscryptocurrency.data.repository.CoinRepository
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import com.omer.nasuhi.loodoscryptocurrency.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class SplashViewModel @ViewModelInject constructor(private val coinRepository: CoinRepository) :
    ViewModel() {

    val firebaseUser: MutableLiveData<Resource<FirebaseUser>> = MutableLiveData()

    val apiStatus: MutableLiveData<Resource<ApiStatusResponse>> = MutableLiveData()

    fun signInFirebase() = viewModelScope.launch {
        safeSignInFirebaseCall()
    }

    fun checkApiStatus(context: Context) = viewModelScope.launch {
        safeCheckApiStatusCall(context)
    }

    private suspend fun safeSignInFirebaseCall() {
        firebaseUser.postValue(Resource.Loading())
        try {
            val authResult = coinRepository.signInFirebase()
            authResult.addOnFailureListener {
            }
            authResult.addOnCompleteListener { result ->
                if (result.isSuccessful && result.result != null) {
                    result.result?.let { authResponse ->
                        if (authResponse.user != null) {
                            firebaseUser.postValue(Resource.Success(authResponse.user!!))
                        } else
                            firebaseUser.postValue(Resource.Error("Could not login to app, try again later"))
                    }
                }
            }
        } catch (t: Throwable) {
            firebaseUser.postValue(Resource.Error("An error occurred: ${t.message}"))
        }
    }

    private suspend fun safeCheckApiStatusCall(context: Context) {
        apiStatus.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(context)) {
                val response = coinRepository.checkApiStatus()
                apiStatus.postValue(handleCheckApiStatusResponse(response))
            } else {
                apiStatus.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    apiStatus.postValue(Resource.Error("Network error"))
                else ->
                    apiStatus.postValue(
                        Resource.Error("An error occurred, please try again")
                    )
            }
        }
    }

    private fun handleCheckApiStatusResponse(response: Response<ApiStatusResponse>): Resource<ApiStatusResponse> {
        if (response.isSuccessful && response.body()?.gecko_says!!.isNotEmpty()) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else {
            response.errorBody()?.let {
                return Resource.Error(it.string())
            }
        }
        return Resource.Error("An error occurred, please try again.\n${response.body()?.gecko_says}")
    }
}