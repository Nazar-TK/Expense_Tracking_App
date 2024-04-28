package com.example.expensestracker.domain.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getLatestTransaction(): Flow<Resource<Transaction>>

    fun getPagingTransactions(limit: Int, offset: Int): Flow<Resource<List<Transaction>>>

    fun addTransaction(transaction: Transaction): Flow<Resource<Boolean>>
}