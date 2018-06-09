package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by aleksandrcikin on 25.04.17.
 *
 */

typealias SubscribeHandler = (Disposable) -> Unit
typealias ErrorHandler = (Throwable) -> Boolean
typealias ResultHandler<T> = (T) -> Unit
typealias CompleteHandler = () -> Unit
internal typealias PureErrorHandler = (Throwable) -> Unit

private class ModernRxObserver<T> internal constructor(
        private val subscribe: SubscribeHandler?,
        private val result: ResultHandler<T>?,
        private val complete: CompleteHandler?,
        private val error: PureErrorHandler?
) : CompletableObserver, MaybeObserver<T>, SingleObserver<T>, Observer<T>,
        org.reactivestreams.Subscriber<T> {

    private var completeHandled = AtomicBoolean(false)

    override fun onSuccess(t: T) {
        result?.invoke(t)
        handleComplete()
    }

    override fun onNext(t: T) {
        result?.invoke(t)
    }

    override fun onSubscribe(subscription: Subscription) {
        subscribe?.invoke(SubscriptionDisposable(subscription))
    }

    override fun onSubscribe(d: Disposable) {
        subscribe?.invoke(d)
    }

    override fun onComplete() {
        handleComplete()
    }

    override fun onError(e: Throwable) {
        error?.invoke(e)
    }

    private fun handleComplete() {
        if (completeHandled.getAndSet(true)) return
        complete?.invoke()
    }

}

private fun <T> subscribeItObserver(
        onSubscribe: SubscribeHandler? = null,
        onResult: ResultHandler<T>? = null,
        onError: ErrorHandler? = null,
        onComplete: CompleteHandler? = null): ModernRxObserver<T> {

    return ModernRxObserver(
            subscribe = onSubscribe,
            result = onResult,
            complete = onComplete,
            error = checkedErrorHandler(onError)
    )

}

private fun checkedErrorHandler(onError: ErrorHandler?): PureErrorHandler {

    val subscriberPackage = BuildConfig.APPLICATION_ID

    val stackTrace = Thread.currentThread().stackTrace

    val lastModernRxLine = stackTrace.indexOfLast {
        it.className.startsWith(subscriberPackage)
    }

    val clearedStackTrace = stackTrace.drop(lastModernRxLine + 1).toTypedArray()

    return lambda@ {

        if (onError?.invoke(it) == true) return@lambda

        RxJavaPlugins.onError(UnhandledErrorException(clearedStackTrace, it))

    }

}

class UnhandledErrorException(stackTrace: Array<StackTraceElement>, cause: Throwable):
        Throwable("Unhandled subscriber error: ${cause.message}", cause) {

    init {
        this.stackTrace = stackTrace
    }

}

class SimpleErrorHandler(
        private val errorHandled: Boolean,
        private val action: (Throwable) -> Unit
): ErrorHandler {

    constructor(errorHandled: Boolean, pureAction: () -> Unit):
            this(errorHandled, { _ -> pureAction() })

    override fun invoke(p1: Throwable): Boolean {
        action(p1)
        return errorHandled
    }

}

fun Completable.modernSubscribe(
        onError: ErrorHandler? = null,
        onComplete: CompleteHandler? = null
) : Disposable {
    val disposable = AtomicReference<Disposable>()
    val observer = subscribeItObserver<Any>(
            onSubscribe = disposable::set,
            onComplete = onComplete,
            onError = onError
    )
    subscribe(observer)
    return disposable.get()
}

fun <T> Maybe<T>.modernSubscribe(
        onError: ErrorHandler? = null,
        onComplete: CompleteHandler? = null,
        onSuccess: ResultHandler<T>? = null
) : Disposable {
    val disposable = AtomicReference<Disposable>()
    val observer = subscribeItObserver(
            onSubscribe = disposable::set,
            onResult = onSuccess,
            onComplete = onComplete,
            onError = onError
    )
    subscribe(observer)
    return disposable.get()
}

fun <T> Single<T>.modernSubscribe(
        onError: ErrorHandler? = null,
        onSuccess: ResultHandler<T>? = null
) : Disposable {
    val disposable = AtomicReference<Disposable>()
    val observer = subscribeItObserver(
            onSubscribe = disposable::set,
            onResult = onSuccess,
            onError = onError
    )
    subscribe(observer)
    return disposable.get()
}

fun <T> Observable<T>.modernSubscribe(
        onError: ErrorHandler? = null,
        onComplete: CompleteHandler? = null,
        onNext: ResultHandler<T>? = null
) : Disposable {
    val disposable = AtomicReference<Disposable>()
    val observer = subscribeItObserver(
            onSubscribe = disposable::set,
            onResult = onNext,
            onComplete = onComplete,
            onError = onError
    )
    subscribe(observer)
    return disposable.get()
}

fun <T> Flowable<T>.modernSubscribe(
        onError: ErrorHandler? = null,
        onComplete: CompleteHandler? = null,
        onNext: ResultHandler<T>? = null
) : SubscriptionDisposable {
    val disposable = AtomicReference<Disposable>()
    val observer = subscribeItObserver(
            onSubscribe = disposable::set,
            onResult = onNext,
            onComplete = onComplete,
            onError = onError
    )
    subscribe(observer)
    return disposable.get() as SubscriptionDisposable
}
