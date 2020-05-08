package dev.sunnyday.modernrx

import java.util.*

object ModernRxUtil {
    
    private val errorInfoMap = WeakHashMap<Throwable, ErrorInfo>()
    
    internal fun putErrorInfo(error: Throwable, info: ErrorInfo) {
        errorInfoMap[error] = info
    }
    
    fun getErrorInfo(error: Throwable): ErrorInfo? {
        return errorInfoMap[error]
    }
    
}

data class ErrorInfo(
    val subscribeStackTrace: List<StackTraceElement>
)