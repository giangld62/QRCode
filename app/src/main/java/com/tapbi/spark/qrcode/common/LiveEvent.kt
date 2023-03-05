package com.tapbi.spark.qrcode.common

import androidx.annotation.MainThread
import androidx.collection.ArraySet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer


class LiveEvent<T> : MediatorLiveData<T>() {
    private val observers = ArraySet<ObserverWrapper<T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper: ObserverWrapper<T> = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val wrapper: ObserverWrapper<T> = ObserverWrapper<T>(observer)
        observers.add(wrapper)
        super.observeForever(wrapper)
    }


    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        if (observers.remove(observer as ObserverWrapper<*>)) {
            super.removeObserver(observer)
            return
        }
        val iterator: MutableIterator<ObserverWrapper<T>> = observers.iterator()
        while (iterator.hasNext()) {
            val wrapper = iterator.next() as ObserverWrapper<in T>
            if (wrapper.observer === observer) {
                iterator.remove()
                super.removeObserver(observer)
                break
            }
        }
    }

    override fun postValue(value: T) {
        for (wrapper in observers) {
            wrapper.newValue()
        }
        super.postValue(value)
    }

    @MainThread
    override fun setValue(value: T?) {
        for (wrapper in observers) {
            wrapper.newValue()
        }
        super.setValue(value)
    }

    private class ObserverWrapper<T>(val observer: Observer<in T>) : Observer<T> {
        private var pending = false
        override fun onChanged(o: T) {
            if (pending) {
                pending = false
                observer.onChanged(o)
            }
        }

        fun newValue() {
            pending = true
        }
    }
}