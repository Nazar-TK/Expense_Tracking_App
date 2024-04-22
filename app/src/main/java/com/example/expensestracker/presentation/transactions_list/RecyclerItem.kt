package com.example.expensestracker.presentation.transactions_list

import com.example.expensestracker.domain.model.Transaction

sealed class RecyclerItem {

    data class Item(val transaction: Transaction): RecyclerItem()
    data class Header(val date: String): RecyclerItem()
}
