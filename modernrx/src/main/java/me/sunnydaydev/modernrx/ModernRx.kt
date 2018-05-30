package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.disposables.Disposable

interface ModernRx {

    val modernRxDisposable: ClosedDisposableBag

    fun Completable.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null
    ) : Disposable = disposeBy(modernRxDisposable).modernSubscribe(onError, onComplete)

    fun <T> Maybe<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposable).modernSubscribe(onError, onComplete, onSuccess)

    fun <T> Single<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposable).modernSubscribe(onError, onSuccess)

    fun <T> Observable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposable).modernSubscribe(onError, onComplete, onNext)

    fun <T> Flowable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : SubscriptionDisposable = disposeBy(modernRxDisposable).modernSubscribe(onError, onComplete, onNext)

}

class ClosedDisposableBag(
        private val bag: DisposableBag
) {

    internal fun track(upstream: Completable): Completable = bag.track(upstream)

    internal fun <T> track(upstream: Single<T>): Single<T> = bag.track(upstream)

    internal fun <T> track(upstream: Maybe<T>): Maybe<T> = bag.track(upstream)

    internal fun <T> track(upstream: Observable<T>): Observable<T> = bag.track(upstream)

    internal fun <T> track(upstream: Flowable<T>): Flowable<T> = bag.track(upstream)

}

internal fun Completable.disposeBy(bag: ClosedDisposableBag): Completable = compose { bag.track(it) }

internal fun <T> Maybe<T>.disposeBy(bag: ClosedDisposableBag): Maybe<T> = compose { bag.track(it) }

internal fun <T> Single<T>.disposeBy(bag: ClosedDisposableBag): Single<T> = compose { bag.track(it) }

internal fun <T> Observable<T>.disposeBy(bag: ClosedDisposableBag): Observable<T> = compose { bag.track(it) }

internal fun <T> Flowable<T>.disposeBy(bag: ClosedDisposableBag): Flowable<T> = compose { bag.track(it) }