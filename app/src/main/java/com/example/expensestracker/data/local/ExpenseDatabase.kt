package com.example.expensestracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensestracker.data.local.entity.BitcoinRateEntity

@Database(
    entities = [BitcoinRateEntity::class],
    version = 1
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract val dao: ExpenseDao
}