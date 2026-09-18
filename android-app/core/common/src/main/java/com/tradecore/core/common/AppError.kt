package com.tradecore.core.common

sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data class Server(val code: String, val message: String) : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
