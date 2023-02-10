package eu.ibagroup.zdevops.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hudson.model.TaskListener
import java.lang.NullPointerException

inline fun <R> runMFTryCatchWrappedQuery(listener: TaskListener, call: () -> R): Result<R> {
    try {
        return Result.success(call())
    } catch (e: Exception) {
        val responseMap: Map<String, Any> = Gson().fromJson(e.message, object : TypeToken<Map<String, Any>>() {}.type)
        var errorContent: Any
        try {
            errorContent = responseMap.get("details") as ArrayList<*>
            errorContent.forEach {listener.logger.println(it)}
        } catch (e: NullPointerException) {
            errorContent = responseMap.get("message") as String
            listener.logger.println(errorContent)
        }
        throw Exception(e)
    }
}