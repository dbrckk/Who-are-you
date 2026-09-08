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

internal object TelemetryPrivacy {
    private const val MaxStringLength = 96
    private const val MaxParams = 16

    fun eventName(value: String): String = value
        .lowercase()
        .filter { it.isLetterOrDigit() || it == '_' }
        .take(48)

    fun params(values: Map<String, Any?>): Map<String, Any> = values
        .asSequence()
        .filter { (key, value) -> key.isNotBlank() && value != null }
        .take(MaxParams)
        .mapNotNull { (key, value) ->
            safeValue(value)?.let { safe -> key.take(48) to safe }
        }
        .toMap()

    private fun safeValue(value: Any): Any? = when (value) {
        is Boolean -> value
        is Byte, is Short, is Int, is Long, is Float, is Double -> value
        is String -> value.take(MaxStringLength)
        is Enum<*> -> value.name.lowercase().take(MaxStringLength)
        else -> null
    }
}

internal object LogEventSink : EventSink {
    private const val tag = "WhoAreYouEvents"

    override fun send(name: String, params: Map<String, Any?>) {
        val safeName = TelemetryPrivacy.eventName(name)
        val safeParams = TelemetryPrivacy.params(params)
        val payload = safeParams.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.d(tag, if (payload.isBlank()) safeName else "$safeName | $payload")
    }

    override fun recordError(throwable: Throwable, context: Map<String, Any?>) {
        Log.e(tag, "non_fatal | ${TelemetryPrivacy.params(context).entries.joinToString(", ") { "${it.key}=${it.value}" }}", throwable)
    }
}

internal class HttpEventSink(private val endpoint: String) : EventSink {
    private val executor = Executors.newSingleThreadExecutor()

    override fun send(name: String, params: Map<String, Any?>) {
        enqueue(
            JSONObject().apply {
                put("type", "event")
                put("name", TelemetryPrivacy.eventName(name))
                put("timestamp_ms", System.currentTimeMillis())
                put("params", JSONObject(TelemetryPrivacy.params(params)))
            }
        )
    }

    override fun recordError(throwable: Throwable, context: Map<String, Any?>) {
        enqueue(
            JSONObject().apply {
                put("type", "non_fatal")
                put("timestamp_ms", System.currentTimeMillis())
                put("error", throwable::class.java.name.take(96))
                put("context", JSONObject(TelemetryPrivacy.params(context)))
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
