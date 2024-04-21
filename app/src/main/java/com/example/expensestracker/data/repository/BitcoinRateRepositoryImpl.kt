package com.example.expensestracker.data.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.data.remote.BitcoinRateApi
import com.example.expensestracker.domain.model.BitcoinRate
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class BitcoinRateRepositoryImpl(private val api: BitcoinRateApi): BitcoinRateRepository {
    override fun getBitcoinRate(): Flow<Resource<BitcoinRate>>  = flow {

        try {
            emit(Resource.Loading())
            val bitcoinRate = api.getBitcoinRate().toBitcoinRate()
            emit(Resource.Success(bitcoinRate))
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error(message = "Could not reach the server. Check your Internet connection."))
        }
    }
}