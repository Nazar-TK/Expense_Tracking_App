package com.example.expensestracker.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensestracker.R
import com.example.expensestracker.databinding.ActivityMainBinding
import com.example.expensestracker.domain.repository.BitcoinRateRepository
import com.example.expensestracker.presentation.new_transaction.TransactionActivity
import com.example.expensestracker.presentation.transactions_list.TransactionAdapter
import com.example.expensestracker.presentation.transactions_list.TransactionsListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private val TAG: String = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TransactionsListViewModel by viewModels()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.bitcoinRateState.collect { rate ->
                // Update TextView with the emitted Bitcoin rate
                rate.let { rate ->
                    binding.tvBitcoinRate.text = rate
                }
            }
        }
        lifecycleScope.launch {
            viewModel.accountBalanceState.collect { rate ->
                // Update TextView with the emitted account balance
                rate.let { rate ->
                    binding.tvBitcoinBalance.text = rate
                }
            }
        }

        updateUI()

        viewModel.getAllTransactions()

        val tr = viewModel.transactionGroups
        Log.d("HERE!!!", tr.value.toString())
        val adapter = TransactionAdapter() // Initialize adapter with empty list
        binding.rcView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcView.adapter = adapter
        adapter.updateList(tr.value)
//        // Start observing the transactionGroups StateFlow
//        val job = lifecycleScope.launch {
//            viewModel.transactionGroups.collect { groups ->
//                // Update the adapter with the new data
//                adapter.transactionGroups = groups
//                adapter.notifyDataSetChanged()
//            }
//        }
        binding.btnRecharge.setOnClickListener {
            createAlertDialog()
        }
        binding.btnAddTransaction.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        binding.btnTest.setOnClickListener {

            viewModel.getAllTransactions()
            val tr = viewModel.transactionGroups
            Log.d("HERE!!!!", tr.value.toString())
            adapter.updateList(tr.value)
        }
    }

    override fun onRestart() {
        super.onRestart()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun createAlertDialog() {
        // Inflate the layout for the dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.recharge_dialog_layout, null)

        // Create an AlertDialog builder
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Recharge Bitcoins")
            .setPositiveButton("Recharge") { dialog, which ->
                // Handle recharge button click
                val etAmount = dialogView.findViewById<EditText>(R.id.etBtcAmount)
                val amountText = etAmount.text.toString()
                if (amountText.isNotEmpty()) {
                    val amount = amountText.toDouble()
                    // Handle recharge with the entered amount
                    viewModel.rechargeBalance(amount)
                    viewModel.getAccountBalance()

                } else {
                    // Show error message if amount is empty
                    Toast.makeText(this, "Please enter an amount of Bitcoins", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
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
    }
}