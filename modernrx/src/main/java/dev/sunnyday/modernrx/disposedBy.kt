package dev.sunnyday.modernrx

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.internal.disposables.DisposableContainer


fun Completable.disposeBy(
    container: DisposableContainer
): Completable = Completable.create { emitter ->
    val dispose = subscribe(emitter::onComplete, emitter::onError)
    
    container.add(dispose)
    
    emitter.setDisposable(Disposables.fromAction {
        dispose.dispose()
        container.delete(dispose)
    })
}

fun <T> Maybe<T>.disposeBy(
    container: DisposableContainer
): Maybe<T> = Maybe.create { emitter ->
    val dispose = subscribe(
        emitter::onSuccess,
        emitter::onError,
        emitter::onComplete
    )
    
    container.add(dispose)
    
    emitter.setDisposable(Disposables.fromAction {
        dispose.dispose()
        container.delete(dispose)
    })
}

fun <T> Single<T>.disposeBy(
    container: DisposableContainer
): Single<T> = Single.create { emitter ->
    val dispose = subscribe(
        emitter::onSuccess,
        emitter::onError
    )
    
    container.add(dispose)
    
    emitter.setDisposable(Disposables.fromAction {
        dispose.dispose()
        container.delete(dispose)
    })
}

fun <T> Observable<T>.disposeBy(
    container: DisposableContainer
): Observable<T> = Observable.create { emitter ->
    val dispose = subscribe(
        emitter::onNext,
        emitter::onError,
        emitter::onComplete
    )
    
    container.add(dispose)
    
    emitter.setDisposable(Disposables.fromAction {
        dispose.dispose()
        container.delete(dispose)
    })
}

fun <T> Flowable<T>.disposeBy(
    container: DisposableContainer,
    mode: BackpressureStrategy = BackpressureStrategy.LATEST
): Flowable<T> = Flowable.create(
    { emitter ->
        val dispose = subscribe(
            emitter::onNext,
            emitter::onError,
            emitter::onComplete
        )
        
        container.add(dispose)
        
        emitter.setDisposable(Disposables.fromAction {
            dispose.dispose()
            container.delete(dispose)
        })
    },
    mode
)

fun Disposable.disposeBy(disposable: DisposableContainer) {
    disposable.add(this)
}