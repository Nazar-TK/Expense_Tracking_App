package com.example.expensestracker.data.remote.dto


import com.google.gson.annotations.SerializedName

data class GBP(
    val code: String,
    val description: String,
    val rate: String,
    @SerializedName("rate_float")
    val rateFloat: Double,
    val symbol: String
)