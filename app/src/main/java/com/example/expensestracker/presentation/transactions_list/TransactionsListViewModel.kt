package com.example.expensestracker.presentation.transactions_list

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.BitcoinRate
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val bitcoinRateRepository: BitcoinRateRepository,
    private val accountBalanceRepository: AccountBalanceRepository
) : ViewModel() {

    private val TAG: String = "TransactionsListViewModel"

    private val _bitcoinRateState = MutableStateFlow("")
    val bitcoinRateState: StateFlow<String> = _bitcoinRateState

    private val _accountBalanceState = MutableStateFlow("0.0")
    val accountBalanceState: StateFlow<String> = _accountBalanceState
    fun getBitcoinRate() {

        val isRateUpdateNeeded = shouldFetchBitcoinRate()

        bitcoinRateRepository.getBitcoinRate(isUpdateNeeded = isRateUpdateNeeded).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _bitcoinRateState.value = "BTC/${result.data?.code ?: ""} = ${ result.data?.rate ?: 0.0}"
                    if (isRateUpdateNeeded) {
                        updateLastFetchTime()
                    }
                    Log.d(TAG, "Update = ${isRateUpdateNeeded} ${_bitcoinRateState.value}")
                }

                is Resource.Error -> {
                    Log.d(TAG, result.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getAccountBalance() {
        accountBalanceRepository.getAccountBalance().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _accountBalanceState.value = "${result.data?.accountBalance} BTC"
                    Log.d(TAG, "User balance: ${result.data?.accountBalance}")
                }

                is Resource.Error -> {
                    Log.d(TAG, result.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun rechargeBalance(amount: Double) {

        accountBalanceRepository.getAccountBalance().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val accountBalance = result.data?.accountBalance
                        ?: 0.0 // Default to 0 if account balance is null
                    val updatedBalance = accountBalance + amount
                    Log.d(TAG, "New balance to be updated: $updatedBalance")
                    accountBalanceRepository.rechargeBalance(AccountBalance(updatedBalance))
                        .onEach {
                            when (it) {
                                is Resource.Success -> {
                                    Log.d(TAG, "Account balance updated successfully")
                                    getAccountBalance()
                                }
                                is Resource.Error -> {
                                    Log.d(TAG, it.message.toString())
                                }
                            }
                        }.launchIn(viewModelScope)
                }
                is Resource.Error -> {
                    Log.d(TAG, result.message.toString())
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun shouldFetchBitcoinRate(): Boolean {
        val lastFetchTimeMillis = sharedPreferences.getLong("lastFetchTimeMillis", 0)
        val currentTimeMillis = System.currentTimeMillis()
        val oneHourMillis = TimeUnit.MINUTES.toMillis(1)
        Log.d(
            TAG,
            "lastFetchTimeMillis = $lastFetchTimeMillis currentTimeMillis = $currentTimeMillis = ${currentTimeMillis - lastFetchTimeMillis}"
        )
        return currentTimeMillis - lastFetchTimeMillis >= oneHourMillis
    }

    private fun updateLastFetchTime() {
        val currentTimeMillis = System.currentTimeMillis()
        sharedPreferences.edit().putLong("lastFetchTimeMillis", currentTimeMillis).apply()
    }

}