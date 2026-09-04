package com.whoareyou.app

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import org.json.JSONObject

internal interface EventSink {
    fun send(name: String, params: Map<String, Any?>)
    fun recordError(throwable: Throwable, context: Map<String, Any?> = emptyMap())
}

internal object LogEventSink : EventSink {
    private const val tag = "WhoAreYouEvents"

    override fun send(name: String, params: Map<String, Any?>) {
        val payload = params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.d(tag, if (payload.isBlank()) name else "$name | $payload")
    }

    override fun recordError(throwable: Throwable, context: Map<String, Any?>) {
        Log.e(tag, "non_fatal | ${context.entries.joinToString(", ") { "${it.key}=${it.value}" }}", throwable)
    }
}

internal class HttpEventSink(private val endpoint: String) : EventSink {
    private val executor = Executors.newSingleThreadExecutor()

    override fun send(name: String, params: Map<String, Any?>) {
        enqueue(
            JSONObject().apply {
                put("type", "event")
                put("name", name)
                put("timestamp_ms", System.currentTimeMillis())
                put("params", JSONObject(params.filterValues { it != null }))
            }
        )
    }

    override fun recordError(throwable: Throwable, context: Map<String, Any?>) {
        enqueue(
            JSONObject().apply {
                put("type", "non_fatal")
                put("timestamp_ms", System.currentTimeMillis())
                put("error", throwable::class.java.name)
                put("message", throwable.message ?: "")
                put("stack", throwable.stackTraceToString().take(12000))
                put("context", JSONObject(context.filterValues { it != null }))
            }
        )
    }

    private fun enqueue(payload: JSONObject) {
        executor.execute {
            runCatching {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 4000
                    readTimeout = 4000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                try {
                    connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
                    connection.responseCode
                } finally {
                    connection.disconnect()
                }
            }.onFailure { Log.w("WhoAreYouEvents", "Telemetry delivery failed", it) }
        }
    }
}
