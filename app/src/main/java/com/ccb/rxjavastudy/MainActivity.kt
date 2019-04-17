package com.ccb.rxjavastudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class MainActivity : AppCompatActivity() {

    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //rxjava+retrofit
        initNet()

    }

    private fun initNet() {
        val getApi by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://mp.weixin.qq.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            retrofit.create(ApiService::class.java)

        }

        val subscribe = getApi.getBaiduNew("https://mp.weixin.qq.com/s/XAZCzxTDc8XISfWzsjpsng")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                Log.e("main","first,$it")
            }
            .observeOn(Schedulers.io())
            .concatMap(Function<String, ObservableSource<String>> {
                return@Function getApi.getBaiduNew("http://news.baidu.com/")
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.e("main","second,$it")
            }
    }

    fun testMap() {
        val subscribe = Observable.create(
            ObservableOnSubscribe<Int> {
                it.onNext(1)
                it.onNext(2)
                it.onNext(3)
            }).map(object : Function<Int, String> {
            override fun apply(t: Int): String {
                return t.toString()
            }
        }).subscribe {
            Log.e("main","map:$it")
        }
    }

    fun testFlatMap(){

    }


    override fun onBackPressed() {
        super.onBackPressed()
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
