package com.example.expensestracker.presentation.new_transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.example.expensestracker.R
import com.example.expensestracker.databinding.ActivityMainBinding
import com.example.expensestracker.databinding.ActivityTransactionBinding
import com.example.expensestracker.presentation.transactions_list.TransactionsListViewModel

class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding

    private val viewModel: NewTransactionViewModel by viewModels()

    val categories = listOf(
        "Groceries",
        "Taxi",
        "Electronics",
        "Restaurant",
        "Other"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }
}