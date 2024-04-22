package com.example.expensestracker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.expensestracker.data.local.ExpenseDatabase
import com.example.expensestracker.data.remote.BitcoinRateApi
import com.example.expensestracker.data.repository.AccountBalanceRepositoryImpl
import com.example.expensestracker.data.repository.BitcoinRateRepositoryImpl
import com.example.expensestracker.data.repository.TransactionRepositoryImpl
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import com.example.expensestracker.domain.repository.TransactionRepository
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
    fun provideWordInfoDatabase(app: Application): ExpenseDatabase {
        return Room.databaseBuilder(
            app,
            ExpenseDatabase::class.java,
            "expense_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBitcoinRateRepository(
        db: ExpenseDatabase,
        api: BitcoinRateApi
    ): BitcoinRateRepository {
        return BitcoinRateRepositoryImpl(db.dao, api)
    }

    @Provides
    @Singleton
    fun provideAccountBalanceRepository(
        db: ExpenseDatabase,
    ): AccountBalanceRepository {
        return AccountBalanceRepositoryImpl(db.dao)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        db: ExpenseDatabase,
    ): TransactionRepository {
        return TransactionRepositoryImpl(db.dao)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("BitcoinRatePrefs", Context.MODE_PRIVATE)
    }
}