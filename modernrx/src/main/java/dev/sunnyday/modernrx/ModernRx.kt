package dev.sunnyday.modernrx

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import dev.sunnyday.modernrx.subscribeIt as modernSubscribe


interface ModernRx {
    
    class Disposer(internal val bag: DisposableBag)
    
    class Simple(bag: DisposableBag = DisposableBag()) : ModernRx {
        
        override val modernRxDisposer: Disposer = Disposer(bag)
        
    }
    
    val modernRxDisposer: Disposer
    
    fun Completable.subscribeIt(
        onError: ErrorHandler = ErrorHandler.justFailed(),
        onComplete: CompleteHandler = { }
    ): Disposable = disposeBy(modernRxDisposer.bag)
        .modernSubscribe(onError, onComplete)
    
    fun <T> Maybe<T>.subscribeIt(
        onError: ErrorHandler = ErrorHandler.justFailed(),
        onComplete: CompleteHandler = { },
        onSuccess: ResultHandler<T> = { }
    ): Disposable = disposeBy(modernRxDisposer.bag)
        .modernSubscribe(onError, onComplete, onSuccess)
    
    fun <T> Single<T>.subscribeIt(
        onError: ErrorHandler = ErrorHandler.justFailed(),
        onSuccess: ResultHandler<T> = { }
    ): Disposable = disposeBy(modernRxDisposer.bag)
        .modernSubscribe(onError, onSuccess)
    
    fun <T> Observable<T>.subscribeIt(
        onError: ErrorHandler = ErrorHandler.justFailed(),
        onComplete: CompleteHandler = { },
        onNext: ResultHandler<T> = { }
    ): Disposable = disposeBy(modernRxDisposer.bag)
        .modernSubscribe(onError, onComplete, onNext)
    
    fun <T> Flowable<T>.subscribeIt(
        onError: ErrorHandler = ErrorHandler.justFailed(),
        onComplete: CompleteHandler = { },
        onNext: ResultHandler<T> = { }
    ): Disposable = disposeBy(modernRxDisposer.bag)
        .modernSubscribe(onError, onComplete, onNext)
    
}