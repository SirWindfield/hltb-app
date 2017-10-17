package de.zerotask.android.hltb.api.hltb

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by Sven on 17. Okt. 2017.
 */
interface HLTBWebAPI {

    @FormUrlEncoded
    @POST("search_main.php")
    fun search(@Field("queryString") query: String): Observable<ResponseBody>

    // Used for actually parsing games
    companion object {
        fun create(): HLTBWebAPI {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl("https://howlongtobeat.com/")
                    .build()

            return retrofit.create(HLTBWebAPI::class.java)
        }
    }
}