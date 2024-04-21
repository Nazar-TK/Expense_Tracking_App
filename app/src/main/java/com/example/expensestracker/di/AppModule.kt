package com.example.expensestracker.di

import com.example.expensestracker.data.remote.BitcoinRateApi
import com.example.expensestracker.data.repository.BitcoinRateRepositoryImpl
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDictionaryApi(): BitcoinRateApi {
        return Retrofit.Builder()
            .baseUrl(BitcoinRateApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BitcoinRateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBitcoinRateRepository(api: BitcoinRateApi): BitcoinRateRepository {
        return BitcoinRateRepositoryImpl(api)
    }
}