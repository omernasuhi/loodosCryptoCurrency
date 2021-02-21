package com.omer.nasuhi.loodoscryptocurrency.data.remote

import com.omer.nasuhi.loodoscryptocurrency.utils.Constants.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: CoinApi by lazy {
            retrofit.create(CoinApi::class.java)
        }
    }
}