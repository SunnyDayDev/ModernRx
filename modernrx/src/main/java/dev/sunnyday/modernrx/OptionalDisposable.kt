package dev.sunnyday.modernrx

import io.reactivex.disposables.Disposable

class OptionalDisposable(
    private val autoDispose: Boolean = true
) : ModernDisposableContainer {
    
    private var _value: Disposable? = null
    
    var value: Disposable?
        get() = _value
        set(value) { add(value) }
    
    @Synchronized
    override fun isDisposed(): Boolean = _value?.isDisposed ?: true
    
    @Synchronized
    override fun dispose() {
        remove(_value)
    }
    
    @Synchronized
    override fun add(d: Disposable?): Boolean {
        if (d === _value) return true
        if (autoDispose) {
            _value?.dispose()
        }
        _value = d
        return true
    }
    
    @Synchronized
    override fun remove(d: Disposable?): Boolean {
        return if (_value === d) {
            _value?.dispose()
            _value = null
            true
        } else {
            false
        }
    }
    
    @Synchronized
    override fun delete(d: Disposable?): Boolean {
        return if (_value === d) {
            _value = null
            true
        } else {
            false
        }
    }
    
}