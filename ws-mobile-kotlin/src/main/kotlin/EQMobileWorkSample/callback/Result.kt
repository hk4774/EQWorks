package EQMobileWorkSample.callback

import java.lang.reflect.ParameterizedType

abstract class Result<T> {
    var tClass =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>

    abstract fun onResultReceived(result: T)
    abstract fun onErrorOccurred(error: Exception)
}