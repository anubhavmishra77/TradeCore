package com.tradecore.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val error: Throwable) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}
