package com.tradecore.feature.auth.presentation

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)
