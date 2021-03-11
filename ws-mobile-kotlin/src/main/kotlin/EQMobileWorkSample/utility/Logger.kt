package EQMobileWorkSample.utility

fun Any.logD(message: String) = Logger.d(javaClass.simpleName, message)

interface LoggerDelegate {
    fun d(tag: String, message: String)
}

interface CompositeLoggerDelegate : LoggerDelegate {
    fun attachDelegate(delegate: LoggerDelegate)
    fun detachDelegate(delegate: LoggerDelegate)
}

object Logger : CompositeLoggerDelegate {

    // not synchronised!
    private val delegates: MutableList<LoggerDelegate> = mutableListOf()

    override fun d(tag: String, message: String) = delegates.forEach { it.d(tag, message) }

    override fun attachDelegate(delegate: LoggerDelegate) {
        delegates.add(delegate)
    }

    override fun detachDelegate(delegate: LoggerDelegate) {
        delegates.remove(delegate)
    }
}