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
typealias ResultHandler<T> = (T) -> Unit
typealias CompleteHandler = () -> Unit
internal typealias PureErrorHandler = (Throwable) -> Unit

private class ModernRxObserver<T> internal  constructor(
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

interface ErrorHandler {

    fun handle(error: Throwable): Boolean

    companion object {

        operator fun invoke(action: (Throwable) -> Boolean): ErrorHandler = object: ErrorHandler {

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

        fun justFailed(action: () -> Unit): ErrorHandler = invoke {
            action()
            false
        }

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

    val subscribeStackTrace = stackTrace.drop(lastModernRxLine + 1)

    return lambda@ {

        ModernRxUtil.putErrorInfo(
                it,
                ErrorInfo(subscribeStackTrace = subscribeStackTrace)
        )

        val handled = onError?.handle(it) == true

        if (!handled) {
            RxJavaPlugins.onError(it)
        }

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
