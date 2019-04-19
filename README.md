# RXJAVA入门学习，实现连续请求

### 用到的操作符
>详情请看文末参考资料

|名称|作用|详解|使用场景|
|:-:|:-:|:-:|:-:|
|observerOn|辅助操作|指定一个观察者在哪个调度器上观察这个Observable|网络请求在主线程观察AndroidSchedulers.mainThread()|
|subscribeOn|辅助操作|指定Observable自身在哪个调度器上执行|网络请求在io线程执行，Schedulers.io()|
|subscribe|辅助操作|操作来自Observable的发射物和通知|网络请求订阅处理观察对象|
|doOnSubscribe|辅助操作|操作符注册一个动作，当观察者订阅它生成的Observable它就会被调用|添加dispose，网络请求做加载动画|
|doOnNext|辅助操作|操作符让你可以注册一个回调，接受发射的数据项|连续请求，接受数据操作，进行下一个请求判断|
|onErrorReturn|错误处理|让Observable遇到错误时发射一个特殊的项并且正常终止。|遇到错误发射消息后面就不执行了|
|onErrorResumeNext|错误处理|让Observable遇到错误时发射一个特殊的项并且正常终止。|虽然错误了，但我们还能发送|
|map|变化操作|操作符对原始Observable发射的每一项数据应用一个你选择的函数，然后返回一个发射这些结果的Observable|简单的数据转换如Int转String|
|flatMap|变化操作|将一个发射数据的Observable变换为多个Observables，然后将它们发射的数据合并后放进一个单独的Observable|这个方法是很有用的，例如，当你有一个这样的Observable：它发射一个数据序列，这些数据本身包含Observable成员或者可以变换为Observable，因此你可以创建一个新的Observable发射这些次级Observable发射的数据的完整集合。|
|concatMap|变化操作|同上并且不会让变换后的Observables发射的数据交错，它按照严格的顺序发射这些数据|同上|
|CompositeDisposable|类|disposable容器|进行断流|
### 示例
```
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
            .subscribe({
                Log.e("main", "secondOnNext")
            }, {
                Log.e("main", "throwable2,${it.message}")
            })
    }

```
- zip：把发送事件结合到一起，比如一个界面需要展示用户的一些信息, 而这些信息分别要从两个服务器接口中获取, 
而只有当两个都获取到了之后才能进行展示, 这个时候就可以用Zip了

>参考资料
[学习专题RxJava专题](https://www.jianshu.com/c/299d0a51fdd4)
，[注册登录逻辑](https://juejin.im/post/5a323801f265da4332279816)
，[RxJava 中的错误处理](https://juejin.im/post/59a66001518825242e5c2906)
，[RxAndroid 中文文档](https://mcxiaoke.gitbooks.io/rxdocs/content/topics/The-RxJava-Android-Module.html)
