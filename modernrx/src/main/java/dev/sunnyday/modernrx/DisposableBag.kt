package dev.sunnyday.modernrx

import io.reactivex.disposables.Disposable
import kotlin.properties.Delegates


class DisposableBag(
    enabled: Boolean = true,
    disposables: Set<Disposable>? = null
) : ModernDisposableContainer {
    
    private val disposables = disposables?.toMutableSet() ?: mutableSetOf()
    
    @get:Synchronized
    @set:Synchronized
    var isEnabled: Boolean by Delegates.observable(true) { _, _, value ->
        if (!value) dispose()
    
        this.disposables
            .filterIsInstance<DisposableBag>()
            .forEach { it.isEnabled = value }
    }
    
    init {
        isEnabled = enabled
    }
    
    @Synchronized
    override fun add(disposable: Disposable): Boolean {
        disposables.add(disposable)
    
        if (disposable is DisposableBag) {
            disposable.isEnabled = isEnabled
        } else {
            if (!isEnabled) {
                disposable.dispose()
            }
        }
        
        return true
    }
    
    @Synchronized
    override fun remove(disposable: Disposable): Boolean {
        val isRemoved = disposables.remove(disposable)
        
        if (isRemoved) {
            disposable.dispose()
        }
        
        return isRemoved
    }
    
    @Synchronized
    override fun delete(disposable: Disposable): Boolean = disposables.remove(disposable)
    
    @Synchronized
    override fun isDisposed(): Boolean = disposables.isEmpty() ||
        disposables.all(Disposable::isDisposed)
    
    @Synchronized
    override fun dispose() {
        val currentDisposables = disposables.toSet()
        
        currentDisposables.forEach(Disposable::dispose)
        
        val disposedForever = currentDisposables
            .filterNot { it is ModernDisposableContainer }
        
        disposables.removeAll(disposedForever)
    }
    
}

