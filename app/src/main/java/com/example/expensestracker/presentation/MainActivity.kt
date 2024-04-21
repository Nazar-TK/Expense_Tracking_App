package com.example.expensestracker.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.expensestracker.R
import com.example.expensestracker.databinding.ActivityMainBinding
import com.example.expensestracker.domain.repository.BitcoinRateRepository
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

        viewModel.getBitcoinRate()

    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getBitcoinRate()
    }
}