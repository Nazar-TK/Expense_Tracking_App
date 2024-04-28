package com.example.expensestracker.presentation.new_transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.expensestracker.core.utils.Category
import com.example.expensestracker.core.utils.TransactionType
import com.example.expensestracker.databinding.ActivityTransactionBinding
import com.example.expensestracker.domain.model.Transaction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding

    private val viewModel: NewTransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categories = Category.values()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Observe transactionSuccess LiveData
        viewModel.transactionSuccess.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                // Transaction was successful, finish activity
                finish()
            } else {
                // Transaction failed
                Toast.makeText(this, "Not enough Bitcoins on your account.", Toast.LENGTH_SHORT).show()
            }
        })

        binding.apply {
            btnAddTransaction.setOnClickListener {
                val amountText = etBtcAmount.text.toString()
                val selectedCategory = categories[spinnerCategory.selectedItemPosition]

                if (amountText.isNotEmpty()) {
                    val amount = amountText.toDouble()
                    // Perform action with amount and category, such as adding the transaction to a database
                    val transaction = Transaction(
                        date = LocalDateTime.now(),
                        btcAmount = amount,
                        category = selectedCategory,
                        type = TransactionType.EXPENSE
                    )
                    viewModel.onAddButtonClick(transaction)
                } else {
                    // Show error message if amount is empty
                    Toast.makeText(this@TransactionActivity, "Please enter an amount of Bitcoins", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}