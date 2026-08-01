package com.example.domain.model

sealed class BackendError {
    data class Network(val message: String) : BackendError()
    data class Timeout(val message: String) : BackendError()
    data class Parsing(val message: String) : BackendError()
    data class Unauthorized(val message: String) : BackendError()
    data class BackendUnavailable(val message: String) : BackendError()
    data class RateLimited(val message: String) : BackendError()
    data class Unknown(val message: String) : BackendError()
}

sealed class BackendResult<out T> {
    data class Success<T>(val data: T) : BackendResult<T>()
    data class Error(val error: BackendError) : BackendResult<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun isSuccess(): Boolean = this is Success
}
