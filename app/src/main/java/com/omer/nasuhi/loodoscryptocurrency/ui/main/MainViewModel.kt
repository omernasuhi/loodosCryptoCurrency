package com.omer.nasuhi.loodoscryptocurrency.ui.main

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.firebase.firestore.SetOptions
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinDetailResponse
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinResponse
import com.omer.nasuhi.loodoscryptocurrency.data.repository.CoinRepository
import com.omer.nasuhi.loodoscryptocurrency.utils.Resource
import com.omer.nasuhi.loodoscryptocurrency.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class MainViewModel @ViewModelInject constructor(private val coinRepository: CoinRepository) :
    ViewModel() {

    var coinList: MutableLiveData<Resource<List<CoinResponse>>> = MutableLiveData()

    val insertCoinList: MutableLiveData<Resource<List<Long>>> = MutableLiveData()

    val filteredCoinList: MutableLiveData<Resource<List<CoinResponse>>> = MutableLiveData()

    val coinDetail: MutableLiveData<Resource<CoinDetailResponse>> = MutableLiveData()

    val addFavoriteCoin: MutableLiveData<Resource<String>> = MutableLiveData()

    val deleteFavoriteCoin: MutableLiveData<Resource<String>> = MutableLiveData()

    val getFavoriteCoins: MutableLiveData<Resource<List<CoinResponse>>> = MutableLiveData()

    fun getCoinsList(context: Context) = viewModelScope.launch {
        safeGetCoinsListCall(context)
    }

    fun insertCoinList(coinList: List<CoinResponse>) = viewModelScope.launch {
        insertCoinListCall(coinList)
    }

    fun filterCoinList(query: String) = viewModelScope.launch {
        filterCoinListCall(query)
    }

    fun getCoinsDetail(context: Context, id: String) = viewModelScope.launch {
        safeGetCoinDetailCall(context, id)
    }

    fun saveFavoriteCoin(coin: HashMap<String, String>) = viewModelScope.launch {
        safeSaveFavoriteCoinCall(coin)
    }

    fun deleteFavoriteCoin(coin: HashMap<String, String>) = viewModelScope.launch {
        safeDeleteFavoriteCoinCall(coin)
    }

    fun getFavoriteCoins() = viewModelScope.launch {
        safeGetFavoriteCoinsCall()
    }

    private suspend fun safeGetCoinsListCall(context: Context) {
        coinList.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(context)) {
                val response = coinRepository.getCoinList()
                coinList.postValue(handleCoinListResponse(response))
            } else {
                coinList.postValue(Resource.Error("No internet connection", emptyList()))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    coinList.postValue(Resource.Error("Network error", emptyList()))
                else ->
                    coinList.postValue(
                        Resource.Error("An error occurred, please try again", emptyList())
                    )
            }
        }
    }

    private fun handleCoinListResponse(response: Response<List<CoinResponse>>): Resource<List<CoinResponse>> {
        if (response.isSuccessful && response.body()?.isNotEmpty()!!) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else {
            response.errorBody()?.let {
                return Resource.Error(it.string(), emptyList())
            }
        }
        return Resource.Error("An error occurred, please try again", emptyList())
    }

    private suspend fun insertCoinListCall(coinList: List<CoinResponse>) {
        insertCoinList.postValue(Resource.Loading())
        try {
            val result = coinRepository.insertCoinListLocalDatabase(coinList)
            if (result.isNotEmpty())
                insertCoinList.postValue(Resource.Success(result))
            else
                insertCoinList.postValue(Resource.Error("An error occurred, please try again"))
        } catch (t: Throwable) {
            insertCoinList.postValue(Resource.Error("An error occurred, please try again: ${t.message}"))
        }
    }

    private suspend fun filterCoinListCall(query: String) {
        filteredCoinList.postValue(Resource.Loading())
        try {
            val simpleSQLiteQuery =
                SimpleSQLiteQuery("SELECT * FROM Coins WHERE coinName LIKE '%$query%' OR coinSymbol LIKE '%$query%'")
            val result = coinRepository.searchCoinByNameOrSymbol(simpleSQLiteQuery)
            if (result.isNotEmpty())
                filteredCoinList.postValue(Resource.Success(result))
            else
                filteredCoinList.postValue(Resource.Error("Coins not found"))
        } catch (t: Throwable) {
            filteredCoinList.postValue(Resource.Error("An error occurred, please try again: ${t.message}"))
        }
    }

    private suspend fun safeGetCoinDetailCall(context: Context, id: String) {
        coinDetail.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(context)) {
                val response = coinRepository.getCoinDetail(id)
                coinDetail.postValue(handleCoinDetailResponse(response))
            } else {
                coinDetail.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    coinDetail.postValue(Resource.Error("Network error"))
                else ->
                    coinDetail.postValue(
                        Resource.Error("An error occurred, please try again")
                    )
            }
        }
    }

    private fun handleCoinDetailResponse(response: Response<CoinDetailResponse>): Resource<CoinDetailResponse> {
        if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
                return Resource.Success(it)
            }
        } else {
            response.errorBody()?.let {
                return Resource.Error(it.string())
            }
        }
        return Resource.Error("An error occurred, please try again")
    }

    private suspend fun safeSaveFavoriteCoinCall(coin: HashMap<String, String>) {
        addFavoriteCoin.postValue(Resource.Loading())
        try {
            val collectionReference = coinRepository.saveFavoriteCoin()
            collectionReference.document(coin["id"]!!)
                .set(coin, SetOptions.merge())
                .addOnSuccessListener {
                    addFavoriteCoin.postValue(Resource.Success("Coin added to favorite"))
                }
                .addOnFailureListener {
                    addFavoriteCoin.postValue(Resource.Error("An error occurred: ${it.message}"))
                }
        } catch (t: Throwable) {
            addFavoriteCoin.postValue(Resource.Error("An error occurred: ${t.message}"))
        }
    }

    private suspend fun safeDeleteFavoriteCoinCall(coin: HashMap<String, String>) {
        deleteFavoriteCoin.postValue(Resource.Loading())
        try {
            val collectionReference = coinRepository.deleteFavoriteCoin()
            collectionReference.document(coin["id"]!!)
                .delete()
                .addOnSuccessListener {
                    deleteFavoriteCoin.postValue(Resource.Success("Coin deleted from favorite"))
                }
                .addOnFailureListener {
                    deleteFavoriteCoin.postValue(Resource.Error("An error occurred: ${it.message}"))
                }
        } catch (t: Throwable) {
            deleteFavoriteCoin.postValue(Resource.Error("An error occurred: ${t.message}"))
        }
    }

    private suspend fun safeGetFavoriteCoinsCall() {
        getFavoriteCoins.postValue(Resource.Loading())
        try {
            val collectionReference = coinRepository.getFavoriteCoins()
            collectionReference.get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        getFavoriteCoins.postValue(Resource.Error("Favorite coins are empty"))
                    } else {
                        val coinList = ArrayList<CoinResponse>()
                        for (snapShot in result) {
                            val id = snapShot.data["id"].toString()
                            val name = snapShot.data["name"].toString()
                            val symbol = snapShot.data["symbol"].toString()

                            val coinResponse = CoinResponse(id, symbol, name)
                            coinList.add(coinResponse)
                        }
                        getFavoriteCoins.postValue(Resource.Success(coinList.toList()))

                    }
                }
                .addOnFailureListener {
                    getFavoriteCoins.postValue(Resource.Error("An error occurred: ${it.message}"))
                }
        } catch (t: Throwable) {
            getFavoriteCoins.postValue(Resource.Error("An error occurred: ${t.message}"))
        }
    }
}