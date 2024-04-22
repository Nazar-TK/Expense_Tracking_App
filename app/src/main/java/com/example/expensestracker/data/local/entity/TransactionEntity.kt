package com.example.expensestracker.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensestracker.core.utils.Category
import com.example.expensestracker.core.utils.TransactionType
import com.example.expensestracker.domain.model.AccountBalance
import com.example.expensestracker.domain.model.Transaction
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Parcelize
data class TransactionEntity(
    @PrimaryKey
    val id: Int? = null,
    val date: LocalDateTime,
    val btcAmount: Double,
    val category: Category?,
    val type: TransactionType
): Parcelable {
    fun toTransaction() : Transaction {
        return Transaction(
            date = date,
            btcAmount = btcAmount,
            category = category,
            type = type
        )
    }
}