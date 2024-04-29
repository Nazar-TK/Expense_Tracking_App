package com.example.expensestracker.data.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.data.local.ExpenseDao
import com.example.expensestracker.domain.model.Transaction
import com.example.expensestracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransactionRepositoryImpl(private val dao: ExpenseDao): TransactionRepository {
    override fun getLatestTransaction(): Flow<Resource<Transaction>> = flow {
        try {
            val transaction = dao.getLatestTransaction().toTransaction()
            emit(Resource.Success(transaction))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not get latest transaction data from database."))
        }
    }

    override fun getPagingTransactions(limit: Int, offset: Int): Flow<Resource<List<Transaction>>> = flow {
        try {
            val transactions = dao.getPagingTransactions(limit, offset).map { it.toTransaction() }
            emit(Resource.Success(transactions))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not get transactions data from database."))
        }
    }

    override fun addTransaction(transaction: Transaction): Flow<Resource<Boolean>> = flow {
        try {
            dao.insertTransaction(transaction.toTransactionEntity())
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not save transaction data to database."))
        }
    }
}