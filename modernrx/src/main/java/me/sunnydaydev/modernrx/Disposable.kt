package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import kotlin.properties.Delegates

/**
 * Created by sashka on 02.03.17.
 *
 * mail: sunnyday.development@gmail.com
 */

class DisposableBag(
        enabled: Boolean = true,
        disposables: Set<Disposable> = emptySet(),
        bags: Set<DisposableBag> = emptySet()
): Disposable {

    private val disposables: MutableSet<Disposable> = disposables.toMutableSet()
    private val bags: MutableSet<DisposableBag> = bags.toMutableSet()

    var enabled: Boolean by Delegates.observable(true) { _, _, value ->
        if (!value) { dispose() }
        bags.forEach { it.enabled = value }
    }

    private val lock = Any()

    init {
        this.enabled = enabled
    }

    fun add(disposable: Disposable) {
        synchronized(lock) {
            if (!enabled) {
                disposable.dispose()
                return
            }
            disposables.add(disposable)
        }
    }

    fun add(bag: DisposableBag) {
        synchronized(lock) {
            bag.enabled = enabled
            bags.add(bag)
        }
    }

    fun remove(disposable: Disposable) {
        synchronized(lock) { disposables.remove(disposable) }
    }

    fun remove(bag: DisposableBag) {
        synchronized(lock) { bags.remove(bag) }
    }

    override fun isDisposed(): Boolean {
        synchronized(lock) {
            return bags.isEmpty() && disposables.isEmpty() ||
                    bags.all { it.isDisposed } && disposables.all { it.isDisposed }
        }
    }

    override fun dispose() {

        synchronized(lock) {

            disposables.forEach { it.dispose() }
            disposables.removeAll { it !is OptionalDisposable }

            bags.forEach { it.dispose() }

        }

    }

    fun trackCompletable() = CompletableTransformer { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackMaybe() = MaybeTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackSingle() = SingleTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackObservable(): ObservableTransformer<T, T> = ObservableTransformer { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackFlowable() = FlowableTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    internal inner class DisposableActor {

        private var disposable: Disposable? = null

        fun onSubscribe(disposable: Disposable) {
            if (enabled) {
                this.disposable = disposable
                add(disposable)
            } else {
                disposable.dispose()
            }
        }

        fun onSubscribe(subscription: Subscription) {
            onSubscribe(SubscriptionDisposable(subscription) as Disposable)
        }

        fun onFinally() {
            disposable?.run { remove(this) }
        }

    }

}

fun Completable.disposeBy(bag: DisposableBag): Completable = compose(bag.trackCompletable())

fun <T> Maybe<T>.disposeBy(bag: DisposableBag): Maybe<T> = compose(bag.trackMaybe())

fun <T> Single<T>.disposeBy(bag: DisposableBag): Single<T> = compose(bag.trackSingle())

fun <T> Observable<T>.disposeBy(bag: DisposableBag): Observable<T> = compose(bag.trackObservable())

fun <T> Flowable<T>.disposeBy(bag: DisposableBag): Flowable<T> = compose(bag.trackFlowable())

fun Disposable.disposeBy(bag: DisposableBag) { bag.add(this) }


class OptionalDisposable(
        private val autoDispose: Boolean = true
): Disposable {

    var value by Delegates.observable<Disposable?>(null) { _, prev, _ ->
        prev ?: return@observable
        if (autoDispose && !prev.isDisposed) prev.dispose()
    }

    fun trackCompletable() = CompletableTransformer { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackMaybe() = MaybeTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackSingle() = SingleTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackObservable() = ObservableTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    fun <T> trackFlowable() = FlowableTransformer<T, T> { upstream ->
        val actor = DisposableActor()
        upstream
                .doOnSubscribe(actor::onSubscribe)
                .doFinally(actor::onFinally)
    }

    override fun isDisposed(): Boolean {
        return value?.isDisposed ?: true
    }

    @Synchronized
    override fun dispose() {
        if (!autoDispose) {
            value?.dispose()
        }
        value = null
    }

    internal inner class DisposableActor {

        fun onSubscribe(disposable: Disposable) {
            value = disposable
        }

        fun onSubscribe(subscription: Subscription) {
            value = SubscriptionDisposable(subscription)
        }

        fun onFinally() {
            value = null
        }

    }

}

fun Completable.disposeBy(disposable: OptionalDisposable): Completable =
        compose(disposable.trackCompletable())

fun <T> Maybe<T>.disposeBy(disposable: OptionalDisposable): Maybe<T> =
        compose(disposable.trackMaybe())

fun <T> Single<T>.disposeBy(disposable: OptionalDisposable): Single<T> =
        compose(disposable.trackSingle())

fun <T> Observable<T>.disposeBy(disposable: OptionalDisposable): Observable<T> =
        compose(disposable.trackObservable())

fun <T> Flowable<T>.disposeBy(disposable: OptionalDisposable): Flowable<T> =
        compose(disposable.trackFlowable())

fun Disposable.disposeBy(disposable: OptionalDisposable) { disposable.value = this }


class SubscriptionDisposable(private val subscription: Subscription) : Disposable, Subscription {

    private var cancelled = false

    override fun dispose() {
        cancel()
    }

    override fun isDisposed(): Boolean = cancelled

    override fun cancel() {
        cancelled = true
        subscription.cancel()
    }

    override fun request(n: Long) {
        cancelled = false
        subscription.request(n)
    }

}
