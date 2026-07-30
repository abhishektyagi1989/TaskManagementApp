package com.taskmaster.data.remote.api

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Error(val code: Int, val message: String?) : ApiResult<Nothing>
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> {
    return when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data))
        is ApiResult.Error -> ApiResult.Error(code, message)
        is ApiResult.Exception -> ApiResult.Exception(throwable)
    }
}
