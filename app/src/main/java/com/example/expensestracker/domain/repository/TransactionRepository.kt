package com.example.expensestracker.domain.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<Resource<List<Transaction>>>

    fun addTransaction(transaction: Transaction): Flow<Resource<Boolean>>
}