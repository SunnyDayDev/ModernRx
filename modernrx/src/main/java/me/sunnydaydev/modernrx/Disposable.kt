package me.sunnydaydev.modernrx

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by sashka on 02.03.17.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
class DisposableBag {

    private val disposables = HashSet<Disposable>()
    private val bags = HashSet<DisposableBag>()

    private val lock = Any()

    constructor()

    constructor(vararg bags: DisposableBag) : super() {
        bags.forEach { addBag(it) }
    }

    fun add(disposable: Disposable) {
        synchronized(lock) { disposables.add(disposable) }
    }

    fun addBag(bag: DisposableBag) {
        synchronized(lock) { bags.add(bag) }
    }

    fun remove(disposable: Disposable) {
        synchronized(lock) { disposables.remove(disposable) }
    }

    fun removeBag(bag: DisposableBag) {
        synchronized(lock) { bags.remove(bag) }
    }

    fun dispose() {

        synchronized(lock) {

            val disposables = this.disposables.toSet()
                    .also { this.disposables.clear() }

            disposables.forEach { it.dispose() }

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


class OptionalDisposable {

    var value: Disposable? = null

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

    @Synchronized
    fun disposeAndClear() {
        value?.dispose()
        value = null
    }

}

fun Completable.disposeBy(disposable: OptionalDisposable): Completable = compose { disposable.track(it) }

fun <T> Maybe<T>.disposeBy(disposable: OptionalDisposable): Maybe<T> = compose { disposable.track(it) }

fun <T> Single<T>.disposeBy(disposable: OptionalDisposable): Single<T> = compose { disposable.track(it) }

fun <T> Observable<T>.disposeBy(disposable: OptionalDisposable): Observable<T> = compose { disposable.track(it) }

fun <T> Flowable<T>.disposeBy(disposable: OptionalDisposable): Flowable<T> = compose { disposable.track(it) }
