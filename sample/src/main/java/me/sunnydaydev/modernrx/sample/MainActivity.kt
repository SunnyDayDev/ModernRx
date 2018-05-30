package me.sunnydaydev.modernrx.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import me.sunnydaydev.modernrx.ClosedDisposableBag
import me.sunnydaydev.modernrx.DisposableBag
import me.sunnydaydev.modernrx.ModernRx
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ModernRx {

    private val globalDisposable = DisposableBag()
    override val modernRxDisposable: ClosedDisposableBag = ClosedDisposableBag(globalDisposable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        Observable.interval(1, TimeUnit.SECONDS)
                .doOnSubscribe { Log.d("Test", "Subscribed") }
                .doOnDispose { Log.d("Test", "Disposed") }
                .subscribeIt {
                    Log.d("Test", "Tick $it")
                }
    }

    override fun onStop() {
        super.onStop()
        globalDisposable.dispose()
    }

}
