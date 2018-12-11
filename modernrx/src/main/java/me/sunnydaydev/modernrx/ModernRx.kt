package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface ModernRx {

    class Disposer(internal val bag: DisposableBag)

    class Simple(bag: DisposableBag = DisposableBag()): ModernRx {

        override val modernRxDisposer: Disposer = Disposer(bag)

    }

    val modernRxDisposer: ModernRx.Disposer

    fun Completable.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null
    ) : Disposable = disposeBy(modernRxDisposer.bag)
            .modernSubscribe(onError, onComplete)

    fun <T> Maybe<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposer.bag)
            .modernSubscribe(onError, onComplete, onSuccess)

    fun <T> Single<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposer.bag)
            .modernSubscribe(onError, onSuccess)

    fun <T> Observable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable = disposeBy(modernRxDisposer.bag)
            .modernSubscribe(onError, onComplete, onNext)

    fun <T> Flowable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : SubscriptionDisposable = disposeBy(modernRxDisposer.bag)
            .modernSubscribe(onError, onComplete, onNext)

}