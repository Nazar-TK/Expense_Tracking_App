package com.example.expensestracker.presentation.transactions_list

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.core.utils.TransactionType
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.Transaction
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import com.example.expensestracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val bitcoinRateRepository: BitcoinRateRepository,
    private val accountBalanceRepository: AccountBalanceRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val TAG: String = "TransactionsListViewModel"

    private val _bitcoinRateState = MutableStateFlow("")
    val bitcoinRateState: StateFlow<String> = _bitcoinRateState

    private val _accountBalanceState = MutableStateFlow("0.0")
    val accountBalanceState: StateFlow<String> = _accountBalanceState

    private val _transactionGroups = MutableStateFlow<List<RecyclerItem>>(emptyList())
    val transactionGroups: StateFlow<List<RecyclerItem>> = _transactionGroups
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
            accountBalanceRepository.getAccountBalance()
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            val accountBalance = result.data?.accountBalance ?: 0.0
                            val updatedBalance = accountBalance + amount
                            updateBalanceAndAddTransaction(updatedBalance, amount)
                        }
                        is Resource.Error -> {
                            Log.d(TAG, result.message.toString())
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun updateBalanceAndAddTransaction(updatedBalance: Double, amount: Double) {
        accountBalanceRepository.updateBalance(AccountBalance(updatedBalance))
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Account balance updated successfully")
                        _accountBalanceState.value = "$updatedBalance BTC"
                        addRechargeTransaction(amount)
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun addRechargeTransaction(amount: Double) {
        val transaction = Transaction(
            date = LocalDateTime.now(),
            btcAmount = amount,
            category = null,
            type = TransactionType.INCOME
        )
        transactionRepository.addTransaction(transaction)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Transaction added successfully")
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun getAllTransactions() {

        transactionRepository.getTransactions()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("HERE", "Transactions got successfully")
                        val res = result!!.data?.let { getGroupedTransactions(it) }
                        _transactionGroups.value = res!!
                        Log.d("HERE!", _transactionGroups.value.toString())
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Function to group transactions by day
    fun getGroupedTransactions(transactions: List<Transaction>): List<RecyclerItem> {
        // Group transactions by date
        val groupedByDate = transactions.groupBy { it.date.toLocalDate() }

        // Create a list to store RecyclerItem objects
        val recyclerItems = mutableListOf<RecyclerItem>()

        // Define a date format
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        // Iterate through each group
        for ((date, transactions) in groupedByDate) {
            // Add a header for the date
            recyclerItems.add(RecyclerItem.Header(date.format(dateFormatter)))

            // Add each transaction as an item
            transactions.forEach { transaction ->
                recyclerItems.add(RecyclerItem.Item(transaction))
            }
        }
        Log.d("HERE!!", recyclerItems.toString())

        return recyclerItems
    }

    // Additional methods to handle if we already need to get bitcoin rate from server or not.
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