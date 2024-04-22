package com.example.expensestracker.domain.model

import com.example.expensestracker.core.utils.Category
import com.example.expensestracker.core.utils.TransactionType
import com.example.expensestracker.data.local.entity.AccountBalanceEntity
import com.example.expensestracker.data.local.entity.TransactionEntity
import java.time.LocalDateTime

data class Transaction(
    val date: LocalDateTime,
    val btcAmount: Double,
    val category: Category?,
    val type: TransactionType
){
    fun toTransactionEntity(): TransactionEntity {
        return TransactionEntity(
            date = date,
            btcAmount = btcAmount,
            category = category,
            type = type
        )
    }
}
