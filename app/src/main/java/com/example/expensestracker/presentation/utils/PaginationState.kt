package com.example.expensestracker.presentation.utils

enum class PaginationState {
    REQUEST_INACTIVE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
    EMPTY,
}