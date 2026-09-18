package com.tradecore.feature.orders.presentation

data class OrderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)
