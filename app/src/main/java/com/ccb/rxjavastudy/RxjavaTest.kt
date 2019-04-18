package com.ccb.rxjavastudy

import android.util.Log
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Created by Ting on 2019/4/17.
 * map,flatmap
 */
fun main() {
//    testMap()
//    testFlatMap()
    testZip()
    Thread.sleep(10000)
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

fun testZip() {
    val observable = Observable.create(ObservableOnSubscribe<Int> {
        it.onNext(1)
        println("emit 1")
        Thread.sleep(1000L)

        it.onNext(2)
        println("emit 2")
        Thread.sleep(1000L)

        it.onNext(3)
        println("emit 3")
        Thread.sleep(1000L)

        it.onNext(4)
        println("emit 4")
        Thread.sleep(1000L)

    }).subscribeOn(Schedulers.io())

    val observable2 = Observable.create(ObservableOnSubscribe<String> {
        it.onNext("A")
        println("emit A")
        Thread.sleep(1000L)

        it.onNext("B")
        println("emit B")
        Thread.sleep(1000L)

        it.onNext("C")
        println("emit C")
        Thread.sleep(1000L)

    }).subscribeOn(Schedulers.io())

    val subscribe = Observable.zip(observable, observable2, object : BiFunction<Int, String, String> {
        override fun apply(t1: Int, t2: String): String {
            return "$t1$t2"
        }

    }).subscribe {
        println(it)
    }
}