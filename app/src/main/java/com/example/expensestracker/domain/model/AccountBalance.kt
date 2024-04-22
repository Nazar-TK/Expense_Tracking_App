package com.example.expensestracker.domain.model

import com.example.expensestracker.data.local.entity.AccountBalanceEntity

data class AccountBalance(
    val accountBalance: Double
) {
    fun toAccountBalanceEntity(): AccountBalanceEntity {
        return AccountBalanceEntity(
            balance = accountBalance
        )
    }
}