package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.disposables.Disposable

interface ModernRxSubscriber {

    val modernRxHandler: ModernRxSubscriber.Handler

    fun Completable.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null
    ) : Disposable = modernRxHandler.subscribeIt(this, onError, onComplete)

    fun <T> Maybe<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = modernRxHandler.subscribeIt(this, onError, onComplete, onSuccess)

    fun <T> Single<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable = modernRxHandler.subscribeIt(this, onError, onSuccess)

    fun <T> Observable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable = modernRxHandler.subscribeIt(this, onError, onComplete, onNext)

    fun <T> Flowable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable = modernRxHandler.subscribeIt(this, onError, onComplete, onNext)

    class Handler(
            private val subscriber: Subscriber,
            private val disposableBag: DisposableBag
    ) {

        fun subscribeIt(
                upstream: Completable,
                onError: ErrorHandler? = null,
                onComplete: CompleteHandler? = null
        ) : Disposable {

            return upstream
                    .disposeBy(disposableBag)
                    .with(subscriber)
                    .subscribe(onError, onComplete)

        }

        fun <T> subscribeIt(
                upstream: Maybe<T>,
                onError: ErrorHandler? = null,
                onComplete: CompleteHandler? = null,
                onSuccess: ResultHandler<T>? = null
        ) : Disposable {

            return upstream.disposeBy(disposableBag)
                    .with(subscriber)
                    .subscribe(onError, onComplete, onSuccess)

        }

        fun <T> subscribeIt(
                upstream: Single<T>,
                onError: ErrorHandler? = null,
                onSuccess: ResultHandler<T>? = null
        ) : Disposable {

            return upstream
                    .disposeBy(disposableBag)
                    .with(subscriber)
                    .subscribe(onError, onSuccess)

        }

        fun <T> subscribeIt(
                upstream: Observable<T>,
                onError: ErrorHandler? = null,
                onComplete: CompleteHandler? = null,
                onNext: ResultHandler<T>? = null
        ) : Disposable {

            return upstream
                    .disposeBy(disposableBag)
                    .with(subscriber)
                    .subscribe(onError, onComplete, onNext)

        }

        fun <T> subscribeIt(
                upstream: Flowable<T>,
                onError: ErrorHandler? = null,
                onComplete: CompleteHandler? = null,
                onNext: ResultHandler<T>? = null
        ) : Disposable {

            return upstream
                    .disposeBy(disposableBag)
                    .with(subscriber)
                    .subscribe(onError, onComplete, onNext)

        }

    }

}