package com.tradecore.backend.common

open class AppException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
