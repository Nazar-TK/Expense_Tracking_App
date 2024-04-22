package com.example.expensestracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensestracker.data.local.entity.AccountBalanceEntity
import com.example.expensestracker.data.local.entity.BitcoinRateEntity
import com.example.expensestracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        BitcoinRateEntity::class,
        AccountBalanceEntity::class,
        TransactionEntity::class
               ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract val dao: ExpenseDao
}