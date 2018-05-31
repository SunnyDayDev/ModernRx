package me.sunnydaydev.modernrx.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Observable
import me.sunnydaydev.modernrx.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ModernRx {

    private val globalDisposable = DisposableBag()
    override val modernRxDisposer: ModernRx.Disposer = ModernRx.Disposer(globalDisposable)

    private val optionalDisposable = OptionalDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnSubscribe { Log.d("Test:ModerRx:Observable", "Subscribed") }
                .doOnDispose { Log.d("Test:ModerRx:Observable", "Disposed") }
                .subscribeIt {
                    Log.d("Test:ModerRx:Observable", "Tick $it")
                }

        Flowable.interval(1, TimeUnit.SECONDS)
                .doOnSubscribe { Log.d("Test:ModerRx:Flowable", "Subscribed") }
                .doOnCancel { Log.d("Test:ModerRx:Flowable", "Cancelled") }
                .subscribeIt {
                    Log.d("Test:ModerRx:Flowable", "Tick $it")
                }
                .request(1000)

        Observable.interval(1, TimeUnit.SECONDS)
                .doOnSubscribe { Log.d("Test:Opt:Observable", "Subscribed") }
                .doOnDispose { Log.d("Test:Opt:Observable", "Disposed") }
                .modernSubscribe {
                    Log.d("Test:Opt:Observable", "Tick $it")
                }
                .disposeBy(optionalDisposable)

        Flowable.interval(1, TimeUnit.SECONDS)
                .doOnSubscribe { Log.d("Test:Opt:Flowable", "Subscribed") }
                .doOnCancel { Log.d("Test:Opt:Flowable", "Cancelled") }
                .disposeBy(optionalDisposable)
                .subscribeIt {
                    Log.d("Test:Opt:Flowable", "Tick $it")
                }
                .request(1000)

    }

    override fun onStop() {
        super.onStop()
        globalDisposable.dispose()
        optionalDisposable.dispose()
    }

}
