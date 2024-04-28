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

    enum class PaginationState {
        REQUEST_INACTIVE,
        LOADING,
        PAGINATING,
        ERROR,
        PAGINATION_EXHAUST,
        EMPTY,
    }

    companion object {
        const val PAGE_SIZE = 5
        const val INITIAL_PAGE = 0
    }

    private val TAG: String = "TransactionsListViewModel"
    private val TAG1: String = "HERE!"

    private val _bitcoinRateState = MutableStateFlow("")
    val bitcoinRateState: StateFlow<String> = _bitcoinRateState

    private val _accountBalanceState = MutableStateFlow("0.0")
    val accountBalanceState: StateFlow<String> = _accountBalanceState

    private val _transactionGroups = MutableStateFlow<List<RecyclerItem>>(emptyList())
    val transactionGroups: StateFlow<List<RecyclerItem>> = _transactionGroups

    private val _pagingState = MutableStateFlow<PaginationState>(PaginationState.LOADING)
    val pagingState: StateFlow<PaginationState> = _pagingState.asStateFlow()

    private var page = INITIAL_PAGE
    private var numOfElementsInCurrentPage = 0
    private val latestTransactionItem = MutableStateFlow<RecyclerItem?>(null)
    var canPaginate by mutableStateOf(false)

    fun getPagingTransactions() {
        if (page == INITIAL_PAGE || (page != INITIAL_PAGE && canPaginate) && _pagingState.value == PaginationState.REQUEST_INACTIVE) {
            _pagingState.update { if (page == INITIAL_PAGE) PaginationState.LOADING else PaginationState.PAGINATING }
        }

        transactionRepository.getPagingTransactions(PAGE_SIZE, page * PAGE_SIZE)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        canPaginate = result.data?.size == PAGE_SIZE

                        val res = result.data?.let { getGroupedTransactions(it) }
                        Log.d(TAG1, "canPaginate $canPaginate ${result.data?.size}")
                        if (page == INITIAL_PAGE) {
                            if (res!!.isEmpty()) {
                                _pagingState.update { PaginationState.EMPTY }
                                Log.d(TAG1, "EMPTY")
                                return@onEach
                            }
                            Log.d(TAG1, "INITIAL_PAGE")
                            _transactionGroups.value = res
                        } else {
                            Log.d(TAG1, "NEW PAGE1 ${_transactionGroups.value}")
                            if(_pagingState.value == PaginationState.PAGINATION_EXHAUST) {
                                Log.d(TAG1, "1 numOfElementsInCurrentPage = $numOfElementsInCurrentPage  result.data?.size = ${result.data?.size}")
                                if (numOfElementsInCurrentPage < (result.data?.size ?: 0)) {
                                    addLatestTransaction()
                                    numOfElementsInCurrentPage = result.data?.size ?: 0
                                    Log.d(TAG1, "2 numOfElementsInCurrentPage = $numOfElementsInCurrentPage  result.data?.size = ${result.data?.size}")
                                }

                            } else
                            _transactionGroups.value = removeItemsWithDuplicateHeaders(_transactionGroups.value.plus(res ?: emptyList()))
                            Log.d(TAG1, "NEW PAGE2 ${_transactionGroups.value}")
                        }

                        _pagingState.update { PaginationState.REQUEST_INACTIVE }

                        if (canPaginate) {
                            page++
                            Log.d(TAG1, "page++ $page")
                        }

                        if (!canPaginate) {
                            Log.d(TAG1, "canPaginate++ PAGINATION_EXHAUST")
                            numOfElementsInCurrentPage = result.data?.size ?: 0
                            _pagingState.update { PaginationState.PAGINATION_EXHAUST }
                        }
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                        Log.d(TAG1, "ERROR")
                        _pagingState.update { if (page == INITIAL_PAGE) PaginationState.ERROR else PaginationState.PAGINATION_EXHAUST }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun clearPaging() {
        page = INITIAL_PAGE
        _pagingState.update { PaginationState.LOADING }
        canPaginate = false
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
                        addLatestTransaction()
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun addLatestTransaction() {
        transactionRepository.getLatestTransaction()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        val res = result.data?.let { getGroupedTransactions(listOf(it)) }
                        _transactionGroups.value = removeItemsWithDuplicateHeaders(res?.plus(_transactionGroups.value)?: emptyList())
                    }

                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun getLatestTransactionItem() {
        transactionRepository.getLatestTransaction()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        latestTransactionItem.value = RecyclerItem.Item(result.data!!)
                        Log.d(TAG1, "getLatestTransactionItem() ${latestTransactionItem.value}")
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Function to group transactions by day
    private fun getGroupedTransactions(transactions: List<Transaction>): List<RecyclerItem> {
        // Group transactions by date
        val groupedByDate = transactions.groupBy { it.date.toLocalDate() }
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