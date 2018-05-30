package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates

/**
 * Created by sashka on 02.03.17.
 *
 * mail: sunnyday.development@gmail.com
 */

class DisposableBag(
        enabled: Boolean = true,
        disposables: Set<Disposable> = mutableSetOf(),
        bags: Set<DisposableBag> = mutableSetOf()
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

    fun track(upstream: Completable): Completable {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Single<T>): Single<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Maybe<T>): Maybe<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Observable<T>): Observable<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) }  }
    }

    fun <T> track(upstream: Flowable<T>): Flowable<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { subscription ->
                    val disposable = SubscriptionDisposable(subscription)
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) }  }
    }

}

fun Completable.disposeBy(bag: DisposableBag): Completable = compose { bag.track(it) }

fun <T> Maybe<T>.disposeBy(bag: DisposableBag): Maybe<T> = compose { bag.track(it) }

fun <T> Single<T>.disposeBy(bag: DisposableBag): Single<T> = compose { bag.track(it) }

fun <T> Observable<T>.disposeBy(bag: DisposableBag): Observable<T> = compose { bag.track(it) }

fun <T> Flowable<T>.disposeBy(bag: DisposableBag): Flowable<T> = compose { bag.track(it) }

fun Disposable.disposedBy(bag: DisposableBag) { bag.add(this) }


class OptionalDisposable(
        autoDispose: Boolean = true
): Disposable {

    var value by Delegates.observable<Disposable?>(null) { _, prev, _ ->
        prev ?: return@observable
        if (autoDispose && !prev.isDisposed) prev.dispose()
    }

    fun track(upstream: Completable): Completable {
        return upstream.doOnSubscribe { value = it }
                .doFinally { value = null }
    }

    fun <T> track(upstream: Maybe<T>): Maybe<T> {
        return upstream.doOnSubscribe { value = it }
                .doFinally { value = null }
    }

    fun <T> track(upstream: Single<T>): Single<T> {
        return upstream.doOnSubscribe { value = it }
                .doFinally { value = null }
    }

    fun <T> track(upstream: Observable<T>): Observable<T> {
        return upstream.doOnSubscribe { value = it }
                .doFinally { value = null }
    }

    fun <T> track(upstream: Flowable<T>): Flowable<T> {
        return upstream.doOnSubscribe { value = SubscriptionDisposable(it) }
                .doFinally { value = null }
    }

    override fun isDisposed(): Boolean {
        return value?.isDisposed ?: true
    }

    @Synchronized
    override fun dispose() {
        value?.dispose()
        value = null
    }

}

fun Completable.disposeBy(disposable: OptionalDisposable): Completable = compose { disposable.track(it) }

fun <T> Maybe<T>.disposeBy(disposable: OptionalDisposable): Maybe<T> = compose { disposable.track(it) }

fun <T> Single<T>.disposeBy(disposable: OptionalDisposable): Single<T> = compose { disposable.track(it) }

fun <T> Observable<T>.disposeBy(disposable: OptionalDisposable): Observable<T> = compose { disposable.track(it) }

fun <T> Flowable<T>.disposeBy(disposable: OptionalDisposable): Flowable<T> = compose { disposable.track(it) }

fun Disposable.disposeBy(disposable: OptionalDisposable) { disposable.value = this }


class SubscriptionDisposable(private val subscription: Subscription) : Disposable, Subscription {

    private var cancelled = true

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
