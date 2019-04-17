package com.ccb.rxjavastudy

import android.util.Log
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Created by Ting on 2019/4/17.
 * map,flatmap
 */
fun main() {
    testMap()
//    testFlatMap()
}

fun testMap() {
    val subscribe = Observable.create(
        ObservableOnSubscribe<Int> {
            it.onNext(1)
            it.onNext(2)
            it.onError(Exception("test"))
            it.onNext(3)
        }).map(object : Function<Int, String> {
        override fun apply(t: Int): String {
            return t.toString()
        }
    }).doOnEach(object : Observer<String> {
        override fun onComplete() {
        }

        override fun onSubscribe(d: Disposable) {
        }

        override fun onNext(t: String) {

        }

        override fun onError(e: Throwable) {
            print("error${e.message}")
        }

    }).concatMap(object : Function<String, ObservableSource<String>> {

        override fun apply(t: String): ObservableSource<String> {
            val list = mutableListOf<String>()
            for (i in 0 until 3) {
                list.add("I am a value $t")
            }
            return Observable.fromIterable(list)
        }

    }).subscribe { t ->
        println("flatMap=$t")
    }
}

fun testFlatMap() {
    val disposable = Observable.create(
        ObservableOnSubscribe<Int> { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onError(Exception("test"))
            emitter.onNext(3)
        }).flatMap(object : Function<Int, ObservableSource<String>> {
        override fun apply(t: Int): ObservableSource<String> {
            val list = mutableListOf<String>()
            for (i in 0 until 3) {
                list.add("I am a value $t")
            }
            return Observable.fromIterable(list).delay(10L, TimeUnit.MILLISECONDS)
        }

    }).subscribe { t ->
        println("flatMap=$t")
    }
}