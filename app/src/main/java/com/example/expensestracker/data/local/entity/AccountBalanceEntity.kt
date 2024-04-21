package com.example.expensestracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.BitcoinRate

@Entity
data class AccountBalanceEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double
){
    fun toAccountBalance() : AccountBalance {
        return AccountBalance(
            accountBalance = balance
        )
    }
}
