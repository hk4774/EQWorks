package EQMobileWorkSample.callback

import okhttp3.Call
import java.lang.reflect.ParameterizedType

abstract class ResponseCallBack<T> {
    var tClass =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>

    abstract fun onFailure(request: Call?, e: Exception?)
    abstract fun onSucceeded(call: Call?, response: T)
}