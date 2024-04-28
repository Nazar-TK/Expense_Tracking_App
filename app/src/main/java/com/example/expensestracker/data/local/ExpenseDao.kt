package com.example.expensestracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensestracker.data.local.entity.AccountBalanceEntity
import com.example.expensestracker.data.local.entity.BitcoinRateEntity
import com.example.expensestracker.data.local.entity.TransactionEntity

@Dao
interface ExpenseDao {

    // Bitcoin rate methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBitcoinRate(rate: BitcoinRateEntity)
    @Query("DELETE FROM bitcoinrateentity")
    suspend fun deleteBitcoinRate()
    @Query ("SELECT * FROM bitcoinrateentity LIMIT 1")
    suspend fun getBitcoinRate() : BitcoinRateEntity?

    // Account balance methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: AccountBalanceEntity)
    @Query("SELECT * FROM accountbalanceentity LIMIT 1")
    suspend fun getBalance(): AccountBalanceEntity?

    // Transactions methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    @Query ("SELECT * FROM transactionentity ORDER BY date DESC LIMIT 1")
    suspend fun getLatestTransaction() : TransactionEntity
    @Query("SELECT * FROM transactionentity ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingTransactions(limit: Int, offset: Int) : List<TransactionEntity>
}