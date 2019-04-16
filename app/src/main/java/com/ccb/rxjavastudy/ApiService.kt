package com.ccb.rxjavastudy

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by Ting on 2019/4/16.
 */
interface ApiService {

    @GET
    fun getBaiduNew(@Url url:String):Observable<String>
}