package com.example.expensestracker.data.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.data.local.ExpenseDao
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AccountBalanceRepositoryImpl(private val dao: ExpenseDao): AccountBalanceRepository {
    override fun getAccountBalance(): Flow<Resource<AccountBalance>> = flow {

        try {
            val balance = dao.getBalance()?.toAccountBalance()
            emit(Resource.Success(balance ?: AccountBalance(0.0)))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not get account balance data from database."))
        }
    }

    override fun updateBalance(accountBalance: AccountBalance): Flow<Resource<Boolean>> = flow {
        try {
            dao.insertBalance(accountBalance.toAccountBalanceEntity())
                emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Could not save account balance data to database."))
        }
    }
}