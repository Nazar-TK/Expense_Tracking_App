package com.example.expensestracker.data.remote

import com.example.expensestracker.data.remote.dto.BitcoinRateDto
import retrofit2.http.GET

interface BitcoinRateApi {

    @GET("/v1/bpi/currentprice.json")
    suspend fun getBitcoinRate(): BitcoinRateDto

    companion object {
        const val BASE_URL = "https://api.coindesk.com/"
    }
}