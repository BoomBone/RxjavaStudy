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
        Log.e("main","启动")
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

        /**
         * 连续请求A-B,例如先注册，注册成功登陆
         * 1.注册成功->登录
         * 2.注册失败：断流，不进行登陆请求
         * 3.注册请求异常：onErrorResumeNext,直接发送异常
         */
        val subscribe = getApi.getBaiduNew("http://gank.io/api/today")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                compositeDisposable.add(it)
            }
            .doOnNext() {
                Log.e("main", "firstOnNext")
                compositeDisposable.clear()
            }
            .onErrorResumeNext(object : Function<Throwable, Observable<String>> {
                override fun apply(t: Throwable): Observable<String> {
                    Log.e("main", "throwable,${t.message}")
                    return Observable.error(t)
                }

            })
            .observeOn(Schedulers.io())
            .concatMap(Function<String, ObservableSource<String>> {
                //注册成功
                val isSuccess = true
                if (isSuccess) {
                    return@Function getApi.getBaiduNew("http://news.baidu.com/")
                } else {
                    return@Function Observable.empty()
                }

            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: String) {
                    Log.e("main", "secondOnNext")
                }

                override fun onError(e: Throwable) {
                    Log.e("main", "throwable2,${e.message}")
                }

            })
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
            Log.e("main", "map:$it")
        }
    }

    fun testFlatMap() {

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
