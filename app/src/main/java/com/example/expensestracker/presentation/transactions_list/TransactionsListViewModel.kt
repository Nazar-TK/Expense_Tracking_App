package com.example.expensestracker.presentation.transactions_list

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.core.utils.TransactionType
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.Transaction
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import com.example.expensestracker.domain.repository.TransactionRepository
import com.example.expensestracker.presentation.utils.PaginationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    companion object {
        private const val PAGE_SIZE = 6
        private const val INITIAL_PAGE = 0
    }

    private val TAG: String = "TransactionsListViewModel"

    private val _bitcoinRateState = MutableStateFlow("")
    val bitcoinRateState: StateFlow<String> = _bitcoinRateState

    private val _accountBalanceState = MutableStateFlow("0.0")
    val accountBalanceState: StateFlow<String> = _accountBalanceState

    private val _transactionGroups = MutableStateFlow<List<RecyclerItem>>(emptyList())
    val transactionGroups: StateFlow<List<RecyclerItem>> = _transactionGroups

    private val _pagingState = MutableStateFlow(PaginationState.LOADING)
    val pagingState: StateFlow<PaginationState> = _pagingState.asStateFlow()

    private var page = INITIAL_PAGE
    private var canPaginate by mutableStateOf(false)

    fun getPagingTransactions() {
        if (page == INITIAL_PAGE || canPaginate && _pagingState.value == PaginationState.REQUEST_INACTIVE) {
            _pagingState.update { if (page == INITIAL_PAGE) PaginationState.LOADING else PaginationState.PAGINATING }
        }

        // To get proper offset we should add number of new transactions, created during current session
        transactionRepository.getPagingTransactions(PAGE_SIZE, page * PAGE_SIZE)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        canPaginate = result.data?.size == PAGE_SIZE

                        val res = result.data?.let { getGroupedTransactions(it) }
                        if (page == INITIAL_PAGE) {
                            if (res!!.isEmpty()) {
                                _pagingState.update { PaginationState.EMPTY }
                                return@onEach
                            }
                            _transactionGroups.value = res
                        } else {
                            _transactionGroups.value = sortAndExtractTransactionsDescending(
                                _transactionGroups.value.plus(res ?: emptyList())
                            )
                        }

                        _pagingState.update { PaginationState.REQUEST_INACTIVE }

                        if (canPaginate) {
                            page++
                        }

                        if (!canPaginate) {
                            Log.d(TAG, "getPagingTransactions(): PAGINATION_EXHAUST")
                            _pagingState.update { PaginationState.PAGINATION_EXHAUST }
                        }
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                        _pagingState.update { if (page == INITIAL_PAGE) PaginationState.ERROR else PaginationState.PAGINATION_EXHAUST }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun getBitcoinRate() {
        val isRateUpdateNeeded = shouldFetchBitcoinRate()

        bitcoinRateRepository.getBitcoinRate(isUpdateNeeded = isRateUpdateNeeded).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _bitcoinRateState.value =
                        "BTC/${result.data?.code ?: ""} = ${result.data?.rate ?: 0.0}"
                    if (isRateUpdateNeeded) {
                        updateLastFetchTime()
                    }
                    Log.d(TAG, "Update = $isRateUpdateNeeded ${_bitcoinRateState.value}")
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
                        Log.d(TAG, "updateBalanceAndAddTransaction(): Account balance updated successfully")
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
                        Log.d(TAG, "addRechargeTransaction(): Transaction added successfully")
                        addLatestTransactionIfItIsNew()
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun addLatestTransactionIfItIsNew() {
        transactionRepository.getLatestTransaction()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val res = result.data?.let { getGroupedTransactions(listOf(it)) }
                        if(!res.isNullOrEmpty()) {
                            _transactionGroups.value = sortAndExtractTransactionsDescending(res.plus(_transactionGroups.value))
                            Log.d(TAG, "addLatestTransactionIfItIsNew(): latest transaction added.")
                        }
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun sortAndExtractTransactionsDescending(items: List<RecyclerItem>): List<RecyclerItem> {
        // Filter out the RecyclerItem objects of type Item and extract their transactions
        val transactions = items.filterIsInstance<RecyclerItem.Item>().map { it.transaction }

        // Sort the transactions by date in descending order
        val sortedTransactions = transactions.sortedByDescending { it.date }
        val uniqueSortedTrans = extractUniqueTransactions(sortedTransactions)
        val groupedTrans = getGroupedTransactions(uniqueSortedTrans)

        return removeItemsWithDuplicateHeaders(groupedTrans)
    }

    private fun extractUniqueTransactions(transactions: List<Transaction>): List<Transaction> {
        val uniqueTransactions = mutableSetOf<Transaction>()
        for (transaction in transactions) {
            uniqueTransactions.add(transaction)
        }
        return uniqueTransactions.toList()
    }

    // Function to group transactions by day
    private fun getGroupedTransactions(transactionList: List<Transaction>): List<RecyclerItem> {
        // Group transactions by date
        val groupedByDate = transactionList.groupBy { it.date.toLocalDate() }
        val recyclerItems = mutableListOf<RecyclerItem>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        val addedHeaders = mutableSetOf<LocalDate>()

        for ((date, transactions) in groupedByDate) {
            // Check if the header for this date was already added
            if (date !in addedHeaders) {
                // Add a header for the date
                recyclerItems.add(RecyclerItem.Header(date.format(dateFormatter)))
                // Remember that we've added a header for this date
                addedHeaders.add(date)
            }
            // Add each transaction as an item
            transactions.forEach { transaction ->
                recyclerItems.add(RecyclerItem.Item(transaction))
            }
        }
        return recyclerItems
    }

    private fun removeItemsWithDuplicateHeaders(items: List<RecyclerItem>): List<RecyclerItem> {
        val uniqueItems = mutableListOf<RecyclerItem>()
        val seenDates = mutableSetOf<String>()
        items.forEach { item ->
            when (item) {
                is RecyclerItem.Header -> {
                    if (item.date !in seenDates) {
                        uniqueItems.add(item)
                        seenDates.add(item.date)
                    }
                }

                is RecyclerItem.Item -> {
                    uniqueItems.add(item)
                }
            }
        }
        return uniqueItems
    }

    // Additional methods to handle if we already need to get bitcoin rate from server or not.
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