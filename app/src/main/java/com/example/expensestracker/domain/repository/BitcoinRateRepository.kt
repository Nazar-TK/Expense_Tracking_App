package com.example.expensestracker.domain.repository

import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.BitcoinRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface BitcoinRateRepository {
    fun getBitcoinRate(isUpdateNeeded: Boolean = false): Flow<Resource<BitcoinRate>>
}