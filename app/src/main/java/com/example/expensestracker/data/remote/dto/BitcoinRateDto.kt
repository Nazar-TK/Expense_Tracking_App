package com.example.expensestracker.data.remote.dto

import com.example.expensestracker.data.local.entity.BitcoinRateEntity
import com.example.expensestracker.domain.model.BitcoinRate


data class BitcoinRateDto(
    val bpi: Bpi,
    val chartName: String,
    val disclaimer: String,
    val time: Time
) {
    fun toBitcoinRateEntity(): BitcoinRateEntity {
        return BitcoinRateEntity(
            code = bpi.uSD.code,
            rate = bpi.uSD.rate
        )
    }
}