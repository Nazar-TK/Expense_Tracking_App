package com.example.expensestracker.data.remote.dto

import com.example.expensestracker.domain.model.BitcoinRate


data class BitcoinRateDto(
    val bpi: Bpi,
    val chartName: String,
    val disclaimer: String,
    val time: Time
) {
    fun toBitcoinRate(): BitcoinRate {
        return BitcoinRate(bpi.uSD.code, bpi.uSD.rate)
    }
}