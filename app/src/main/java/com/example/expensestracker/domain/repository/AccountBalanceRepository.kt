package com.example.expensestracker.domain.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.BitcoinRate
import kotlinx.coroutines.flow.Flow

interface AccountBalanceRepository {

    fun getAccountBalance(): Flow<Resource<AccountBalance>>

    fun rechargeBalance(accountBalance: AccountBalance): Flow<Resource<Boolean>>
}