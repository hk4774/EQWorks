package EQMobileWorkSample.network

import EQMobileWorkSample.callback.ResponseCallBack
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.jetbrains.annotations.Nullable
import java.io.IOException


public class NetworkManager {
    enum class PostThread {
        UI_THREAD, WORK_THREAD
    }

    private var mOkHttpClient: OkHttpClient? = null
    private val mGson = Gson()

    private object HOLDER {
        val INSTANCE = NetworkManager()
    }

    companion object {
        val instance: NetworkManager by lazy { HOLDER.INSTANCE }
    }

    fun setOkHttpClient(@Nullable okHttpClient: OkHttpClient?) {
        if (okHttpClient == null) {
            return
        }
        mOkHttpClient = okHttpClient
    }


    fun <T> postAsync(
        url: String?,
        data: String?,
        postThread: PostThread,
        responseCallBack: ResponseCallBack<T>
    ) {
        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, "{\"data\":\"${data.toString()}\"}")

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val runOnUIThread = PostThread.UI_THREAD == postThread
        executeRequest(request, runOnUIThread, responseCallBack)
    }


    private fun <T> executeRequest(
        request: Request,
        postToUIThread: Boolean,
        responseCallBack: ResponseCallBack<T>
    ) {
        if (mOkHttpClient == null) mOkHttpClient = OkHttpClient()

        val newCall: Call = mOkHttpClient!!.newCall(request)
        newCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                postFailInformation(call, e, postToUIThread, responseCallBack)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response?) {
                val result: T
                if (response != null && !response.isSuccessful()) {
                    postFailInformation(
                        call,
                        Exception(
                            java.lang.String.format(
                                "request failed , code is %s",
                                response.code()
                            )
                        ),
                        postToUIThread,
                        responseCallBack
                    )
                    return
                }
                try {
                    if (String::class.java == responseCallBack.tClass) {
                        result = response?.body()?.string() as T
                    } else {
                        result =
                            mGson.fromJson(response?.body()?.string(), responseCallBack.tClass) as T
                    }
                    postSuccessResult(call, result, postToUIThread, responseCallBack)
                } catch (e: Exception) {
                    postFailInformation(call, e, postToUIThread, responseCallBack)
                }
            }
        })
    }

    private fun <T> postSuccessResult(
        call: Call,
        result: T,
        postToUIThread: Boolean,
        responseCallBack: ResponseCallBack<T>
    ) {
        if (!postToUIThread) {
            responseCallBack.onSucceeded(call, result)
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            responseCallBack.onSucceeded(call, result)
        }
    }

    private fun postFailInformation(
        call: Call,
        e: Exception,
        postToUIThread: Boolean,
        responseCallBack: ResponseCallBack<*>
    ) {
        if (!postToUIThread) {
            responseCallBack.onFailure(call, e)
            return
        }
        // Launch a coroutine that by default goes to the main thread
        GlobalScope.launch(Dispatchers.Main) {
            responseCallBack.onFailure(call, e)
        }
    }

    fun cancelRequest(@Nullable url: String?) {
        for (call in mOkHttpClient!!.dispatcher().queuedCalls()) {
            if (call != null && call.request().tag().toString().equals(url)) {
                call.cancel()
            }
        }
    }


    fun cancelAllRequests() {
        mOkHttpClient!!.dispatcher().cancelAll()
    }

}