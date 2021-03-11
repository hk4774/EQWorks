package EQMobileWorkSample

import EQMobileWorkSample.callback.ResponseCallBack
import EQMobileWorkSample.callback.Result
import EQMobileWorkSample.network.NetworkManager
import EQMobileWorkSample.utility.logD
import Response
import okhttp3.Call
import okhttp3.OkHttpClient


public data class LocationEvent(
    val lat: Float,
    val lon: Float,
    val time: Long = System.currentTimeMillis(),
    val ext: String = ""
)

public class Library private constructor() {

    private var LOG_URL: String = "https://httpbin.org/post"
    private var LOG_EXCEPTION_URL: String = "https://httpbin.org/anything"

    private object HOLDER {
        val INSTANCE = Library()
    }

    companion object {
        val instance: Library by lazy { HOLDER.INSTANCE }
    }

    fun setUp(okHttpClient: OkHttpClient) {
        NetworkManager.instance.setOkHttpClient(okHttpClient)
    }

    fun log(event: LocationEvent, result: Result<Response>) {
        // POST to API Server
        NetworkManager.instance.postAsync<Response>(
            LOG_URL,
            event.toString(),
            NetworkManager.PostThread.UI_THREAD,
            object : ResponseCallBack<Response>() {
                override fun onFailure(request: Call?, e: Exception?) {
                    e?.let {
                        logException(e)
                        result.onErrorOccurred(e)
                    }
                }

                override fun onSucceeded(call: Call?, response: Response) {
                    result.onResultReceived(response)
                }
            }
        )
    }

    fun logException(e: Exception) {
        NetworkManager.instance.postAsync(
            LOG_EXCEPTION_URL,
            e.localizedMessage,
            NetworkManager.PostThread.UI_THREAD,
            object : ResponseCallBack<Response>() {
                override fun onFailure(request: Call?, e: Exception?) {
                    // No need to do anything here, retry logic can be written in okhttp-client itself
                }

                override fun onSucceeded(call: Call?, response: Response) {
                    logD("Error log submitted succesfully")
                }
            }
        )
    }
}

