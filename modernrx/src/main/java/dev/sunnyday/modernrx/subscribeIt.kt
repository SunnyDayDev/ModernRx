package dev.sunnyday.modernrx

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins

/**
 * Created by aleksandrcikin on 25.04.17.
 *
 */

typealias ResultHandler<T> = (T) -> Unit
typealias CompleteHandler = () -> Unit
internal typealias PureErrorHandler = (Throwable) -> Unit

interface ErrorHandler {
    
    fun handle(error: Throwable): Boolean
    
    companion object {
        
        operator fun invoke(action: (Throwable) -> Boolean): ErrorHandler = object : ErrorHandler {
            
            override fun handle(error: Throwable): Boolean = action(error)
            
        }
        
        fun handled(action: (Throwable) -> Unit): ErrorHandler = invoke {
            action(it)
            true
        }
        
        fun justHandled(action: () -> Unit): ErrorHandler = invoke {
            action()
            true
        }
        
        fun failed(action: (Throwable) -> Unit): ErrorHandler = invoke {
            action(it)
            false
        }
        
        fun justFailed(action: (() -> Unit)? = null): ErrorHandler = invoke {
            action?.invoke()
            false
        }
        
    }
    
}

fun Completable.subscribeIt(
    onError: ErrorHandler = ErrorHandler.justFailed(),
    onComplete: CompleteHandler = { }
): Disposable = subscribe(
    Action(onComplete),
    Consumer(checkedErrorHandler(onError))
)

fun <T> Maybe<T>.subscribeIt(
    onError: ErrorHandler = ErrorHandler.justFailed(),
    onComplete: CompleteHandler = { },
    onSuccess: ResultHandler<T> = { }
): Disposable = subscribe(
    Consumer(onSuccess),
    Consumer(checkedErrorHandler(onError)),
    Action(onComplete)
)

fun <T> Single<T>.subscribeIt(
    onError: ErrorHandler = ErrorHandler.justFailed(),
    onSuccess: ResultHandler<T> = { }
): Disposable = subscribe(
    Consumer(onSuccess),
    Consumer(checkedErrorHandler(onError))
)

fun <T> Observable<T>.subscribeIt(
    onError: ErrorHandler = ErrorHandler.justFailed(),
    onComplete: CompleteHandler = { },
    onNext: ResultHandler<T> = { }
): Disposable = subscribe(
    Consumer(onNext),
    Consumer(checkedErrorHandler(onError)),
    Action(onComplete)
)

fun <T> Flowable<T>.subscribeIt(
    onError: ErrorHandler = ErrorHandler.justFailed(),
    onComplete: CompleteHandler = { },
    onNext: ResultHandler<T> = { }
): Disposable = subscribe(
    Consumer(onNext),
    Consumer(checkedErrorHandler(onError)),
    Action(onComplete)
)

private fun checkedErrorHandler(onError: ErrorHandler): PureErrorHandler {
    val stackTrace = Thread.currentThread().stackTrace
    
    val lastModernRxLine = stackTrace.indexOfLast {
        it.className.startsWith("dev.sunnyday.modernrx")
    }
    
    val subscribeStackTrace = stackTrace.drop(lastModernRxLine + 1)
    
    return lambda@{
        ModernRxUtil.putErrorInfo(it, ErrorInfo(subscribeStackTrace = subscribeStackTrace))
        
        val handled = onError.handle(it)
        
        if (!handled) {
            RxJavaPlugins.onError(it)
        }
    }
}