package me.sunnydaydev.modernrx

import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * Created by sunny on 14.09.17.
 * mail: mail@sunnydaydev.me
 */

class SubscriptionDisposable(private val subscription: Subscription) : Disposable {

    private var cancelled = false

    override fun dispose() {

        if (cancelled) return
        cancelled = true

        subscription.cancel()
    }

    override fun isDisposed(): Boolean = cancelled

}
