package com.example.expensestracker.presentation.new_transaction

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensestracker.core.utils.Resource
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.Transaction
import com.example.expensestracker.domain.repository.AccountBalanceRepository
import com.example.expensestracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewTransactionViewModel @Inject constructor(
    private val accountBalanceRepository: AccountBalanceRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val TAG: String = "NewTransactionViewModel"

    private val _transactionSuccess = MutableLiveData<Boolean>()
    val transactionSuccess: LiveData<Boolean> = _transactionSuccess
    fun onAddButtonClick(transaction: Transaction) {
        viewModelScope.launch {
            handleAccountBalance(transaction)
        }
    }

    private fun handleAccountBalance(transaction: Transaction) {
        accountBalanceRepository.getAccountBalance()
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        handleBalanceSuccess(result.data?.accountBalance ?: 0.0, transaction)
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleBalanceSuccess(accountBalance: Double, transaction: Transaction) {
        if (accountBalance >= transaction.btcAmount) {
            val updatedBalance = accountBalance - transaction.btcAmount
            updateBalanceAndAddTransaction(updatedBalance, transaction)
            _transactionSuccess.postValue(true)
        } else {
            Log.d(TAG, "handleBalanceSuccess(): Insufficient account balance")
            _transactionSuccess.postValue(false)
        }
    }

    private fun updateBalanceAndAddTransaction(updatedBalance: Double, transaction: Transaction) {
        accountBalanceRepository.updateBalance(AccountBalance(updatedBalance))
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "updateBalanceAndAddTransaction(): Account balance updated successfully")
                        addTransaction(transaction)
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun addTransaction(transaction: Transaction) {
        transactionRepository.addTransaction(transaction)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "addTransaction(): Transaction added successfully")
                    }
                    is Resource.Error -> {
                        Log.d(TAG, result.message.toString())
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}