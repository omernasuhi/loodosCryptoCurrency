package com.omer.nasuhi.loodoscryptocurrency.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omer.nasuhi.loodoscryptocurrency.adapters.CoinListAdapter
import com.omer.nasuhi.loodoscryptocurrency.data.CoinDao
import com.omer.nasuhi.loodoscryptocurrency.data.CoinDatabase
import com.omer.nasuhi.loodoscryptocurrency.data.remote.CoinApi
import com.omer.nasuhi.loodoscryptocurrency.data.remote.FirebaseHelper
import com.omer.nasuhi.loodoscryptocurrency.data.remote.RetrofitInstance
import com.omer.nasuhi.loodoscryptocurrency.data.repository.CoinRepository
import com.omer.nasuhi.loodoscryptocurrency.ui.main.MainViewModel
import com.omer.nasuhi.loodoscryptocurrency.ui.main.splash.SplashViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object MainAppModule {

    @Singleton
    @Provides
    fun provideFirebaseHelper(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): FirebaseHelper = FirebaseHelper(firebaseAuth, firebaseFirestore)

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideRetrofitApi(): CoinApi = RetrofitInstance.api

    @Provides
    fun provideSplashViewModel(coinRepository: CoinRepository): SplashViewModel =
        SplashViewModel(coinRepository)

    @Provides
    fun provideMainViewModel(coinRepository: CoinRepository): MainViewModel =
        MainViewModel(coinRepository)

    @Provides
    fun provideCoinListAdapter(): CoinListAdapter = CoinListAdapter()

    @Singleton
    @Provides
    fun provideCoinDatabase(@ApplicationContext appContext: Context) =
        CoinDatabase.getDatabase(appContext)

    @Singleton
    @Provides
    fun provideCoinDao(coinDatabase: CoinDatabase) = coinDatabase.coinDao()

    @Singleton
    @Provides
    fun provideCoinRepository(coinApi: CoinApi, firebaseHelper: FirebaseHelper, coinDao: CoinDao) =
        CoinRepository(coinApi, firebaseHelper, coinDao)
}