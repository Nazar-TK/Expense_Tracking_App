package com.example.expensestracker.presentation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensestracker.R
import com.example.expensestracker.databinding.ActivityMainBinding
import com.example.expensestracker.presentation.new_transaction.TransactionActivity
import com.example.expensestracker.presentation.transactions_list.TransactionAdapter
import com.example.expensestracker.presentation.transactions_list.TransactionsListViewModel
import com.example.expensestracker.presentation.utils.PaginationState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TransactionsListViewModel by viewModels()
    private var job: Job? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.bitcoinRateState.collect { rate ->
                // Update TextView with the emitted Bitcoin rate
                binding.tvBitcoinRate.text = rate
            }
        }
        lifecycleScope.launch {
            viewModel.accountBalanceState.collect { rate ->
                // Update TextView with the emitted account balance
                binding.tvBitcoinBalance.text = rate
            }
        }
        updateUI()
        viewModel.getPagingTransactions()
        setupRecyclerView()

        binding.btnRecharge.setOnClickListener {
            createAlertDialog()
        }
        binding.btnAddTransaction.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rcView.layoutManager = layoutManager
        val adapter = TransactionAdapter()

        // Observe the transactionGroups StateFlow
        job = lifecycleScope.launch {
            viewModel.transactionGroups.collect { transactionGroups ->
                // Update the RecyclerView adapter with the new data
                adapter.updateList(transactionGroups)
            }
        }

        binding.rcView.adapter = adapter

        binding.rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutMgr = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutMgr.findLastVisibleItemPosition()
                val totalItemCount = layoutMgr.itemCount
                if (lastVisibleItemPosition == totalItemCount - 1) {
                    if (viewModel.pagingState.value == PaginationState.REQUEST_INACTIVE) {
                        viewModel.getPagingTransactions()
                    }
                }
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel() // Cancel the observation job when activity is destroyed
    }

    private fun createAlertDialog() {
        // Inflate the layout for the dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.recharge_dialog_layout, null)

        // Create an AlertDialog builder
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Recharge Bitcoins")
            .setPositiveButton("Recharge") { _, _ ->
                // Handle recharge button click
                val etAmount = dialogView.findViewById<EditText>(R.id.etBtcAmount)
                val amountText = etAmount.text.toString()
                if (amountText.isNotEmpty()) {
                    val amount = amountText.toDouble()
                    // Handle recharge with the entered amount
                    viewModel.rechargeBalance(amount)
                } else {
                    // Show error message if amount is empty
                    Toast.makeText(this, "Please enter an amount of Bitcoins", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle cancel button click
                dialog.dismiss()
            }

        // Create the AlertDialog
        val alertDialog = alertDialogBuilder.create()

        alertDialog.show()
    }

    private fun updateUI() {
        viewModel.getBitcoinRate()
        viewModel.getAccountBalance()
        viewModel.addLatestTransactionIfItIsNew()
    }
}