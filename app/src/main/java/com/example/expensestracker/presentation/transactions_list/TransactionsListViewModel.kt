package com.example.expensestracker.presentation.transactions_list

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.BitcoinRate
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
    private val bitcoinRateRepository: BitcoinRateRepository
) : ViewModel() {

    private val TAG: String = "TransactionsListViewModel"

    private val _bitcoinRateState = MutableStateFlow("")
    val bitcoinRateState: StateFlow<String> = _bitcoinRateState

    fun getBitcoinRate() {

        if (shouldFetchBitcoinRate()) {
            bitcoinRateRepository.getBitcoinRate().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _bitcoinRateState.value = formatBitcoinRate(
                            BitcoinRate(
                                result.data?.code ?: "",
                                result.data?.rate ?: ""
                            )
                        )
                        updateLastFetchTime()
                        Log.d(TAG, "Update ${_bitcoinRateState.value}")
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun formatBitcoinRate(bitcoinRate: BitcoinRate): String {
        val formattedRate = bitcoinRate.rate.substringBefore(".")
        return "BTC/${bitcoinRate.code} = $formattedRate"
    }

    private fun shouldFetchBitcoinRate(): Boolean {
        val lastFetchTimeMillis = sharedPreferences.getLong("lastFetchTimeMillis", 0)
        val currentTimeMillis = System.currentTimeMillis()
        val oneHourMillis = TimeUnit.MINUTES.toMillis(1)

        return currentTimeMillis - lastFetchTimeMillis >= oneHourMillis
    }

    private fun updateLastFetchTime() {
        val currentTimeMillis = System.currentTimeMillis()
        sharedPreferences.edit().putLong("lastFetchTimeMillis", currentTimeMillis).apply()
    }

}