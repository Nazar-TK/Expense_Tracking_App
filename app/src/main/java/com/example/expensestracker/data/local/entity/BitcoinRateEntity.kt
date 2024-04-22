package com.example.expensestracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensestracker.domain.model.BitcoinRate

@Entity
data class BitcoinRateEntity(
    @PrimaryKey val id: Int = 1,
    val code: String?,
    val rate: String?
) {
    fun toBitcoinRate() : BitcoinRate {
        return BitcoinRate(
            code = code ?: "",
            rate = rate ?: "",
        )
    }
}
