package com.example.expensestracker.data.repository

import android.util.Log
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.data.local.ExpenseDao
import com.example.expensestracker.domain.model.Transaction
import com.example.expensestracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionRepositoryImpl(private val dao: ExpenseDao): TransactionRepository {
    override fun getTransactions(): Flow<Resource<List<Transaction>>> = flow {
        try {
            val transactions = dao.getTransactions().map { it.toTransaction() }
            Log.d("TransactionsListViewModel", "Transactions are: $transactions")
            emit(Resource.Success(transactions))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not get transactions data from database."))
        }
    }

    override fun addTransaction(transaction: Transaction): Flow<Resource<Boolean>> = flow {
        try {
            dao.insertTransaction(transaction.toTransactionEntity())
            Log.d("TransactionsListViewModel", "transaction added")
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Log.d("TransactionsListViewModel", "Transaction NOT added")
            emit(Resource.Error(message = "Could not save transaction data to database."))
        }
    }
}