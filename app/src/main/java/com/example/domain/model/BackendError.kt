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

/** Human-readable message for surfacing backend failures in the UI. */
fun BackendError.userMessage(): String = when (this) {
    is BackendError.Network -> message
    is BackendError.Timeout -> message
    is BackendError.Parsing -> message
    is BackendError.Unauthorized -> message
    is BackendError.BackendUnavailable -> message
    is BackendError.RateLimited -> message
    is BackendError.Unknown -> message
}
