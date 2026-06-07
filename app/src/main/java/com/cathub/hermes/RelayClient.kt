package com.cathub.hermes

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cathub.voice.AudioPlayer
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString

/**
 * WebSocket client for Cat Hub → Hermes relay.
 * Minimal version — just voice + responses.
 */
object RelayClient {
    private const val TAG = "RelayClient"
    private const val SERVER = "ws://100.111.44.87:8766"
    private const val TOKEN = "7454FD"

    private val client = OkHttpClient.Builder()
        .pingInterval(5, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var scope: CoroutineScope? = null
    private var reconnectJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile var isConnected = false
        private set
    @Volatile private var isConnecting = false
    @Volatile private var shouldReconnect = false
    private var retryCount = 0

    var onConnectionChanged: ((Boolean, String) -> Unit)? = null
    var onResponseText: ((String) -> Unit)? = null
    var onSpeakingChanged: ((Boolean) -> Unit)? = null
    var onProcessing: (() -> Unit)? = null
    var onTranscript: ((String, Boolean) -> Unit)? = null

    fun connect() {
        if (isConnected || isConnecting) return
        shouldReconnect = true
        isConnecting = true
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        doConnect()
    }

    fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "Bye")
        webSocket = null
        scope?.cancel()
        scope = null
        isConnected = false
        isConnecting = false
        AudioPlayer.release()
        postMain { onConnectionChanged?.invoke(false, "Disconnected") }
    }

    fun sendAudio(data: ByteArray): Boolean {
        val ws = webSocket ?: return false
        if (!isConnected) return false
        return try {
            ws.send(ByteString.of(*data))
            true
        } catch (e: Exception) {
            Log.w(TAG, "sendAudio failed: ${e.message}")
            false
        }
    }

    fun sendVoiceStart() {
        sendJson("""{"type":"voice_start"}""")
    }

    fun sendVoiceStop() {
        sendJson("""{"type":"voice_stop"}""")
    }

    fun sendChat(text: String) {
        if (!isConnected) return
        val json = org.json.JSONObject().apply {
            put("type", "chat")
            put("text", text)
            put("request_id", "chat_${System.currentTimeMillis()}")
        }
        try { webSocket?.send(json.toString()) } catch (_: Exception) {}
    }

    private fun sendJson(json: String) {
        try { webSocket?.send(json) } catch (_: Exception) {}
    }

    private fun doConnect() {
        val wsUrl = "$SERVER/ws?token=$TOKEN&device_id=cat-hub&model=FireHD10&brand=Amazon"
        Log.i(TAG, "Connecting to $wsUrl")
        postMain { onConnectionChanged?.invoke(false, "Connecting...") }

        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "Connected!")
                isConnected = true
                isConnecting = false
                retryCount = 0
                postMain { onConnectionChanged?.invoke(true, "Connected") }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope?.launch { handleMessage(text) }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Binary = TTS audio
                scope?.launch {
                    try {
                        AudioPlayer.play(bytes.toByteArray())
                        postMain { onSpeakingChanged?.invoke(true) }
                    } catch (e: Exception) {
                        Log.w(TAG, "Audio error: ${e.message}")
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "Closed: $code")
                isConnected = false
                isConnecting = false
                postMain { onConnectionChanged?.invoke(false, "Disconnected") }
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Failed: ${t.message}")
                isConnected = false
                isConnecting = false
                postMain { onConnectionChanged?.invoke(false, "Connection failed") }
                scheduleReconnect()
            }
        })
    }

    private suspend fun handleMessage(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            val msgType = json.get("type")?.asString ?: return

            when (msgType) {
                "ping" -> {}
                "transcript" -> {
                    val txt = json.get("text")?.asString ?: ""
                    val isFinal = json.get("final")?.asBoolean ?: false
                    postMain { onTranscript?.invoke(txt, isFinal) }
                }
                "processing" -> {
                    postMain { onProcessing?.invoke() }
                }
                "response" -> {
                    val responseText = json.get("text")?.asString ?: ""
                    if (responseText.isNotBlank()) {
                        postMain { onResponseText?.invoke(responseText) }
                    }
                }
                "speaking_start" -> {
                    postMain { onSpeakingChanged?.invoke(true) }
                }
                "speaking_end" -> {
                    postMain { onSpeakingChanged?.invoke(false) }
                }
                "status" -> {
                    Log.i(TAG, "Status: ${json.get("data")?.asString}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Parse error: ${e.message}")
        }
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect || retryCount >= 20) return
        retryCount++
        val delay = minOf(1000L * (1 shl retryCount), 30_000L)
        Log.i(TAG, "Reconnect in ${delay}ms (attempt $retryCount)")
        reconnectJob = scope?.launch {
            delay(delay)
            if (shouldReconnect && !isConnected) {
                isConnecting = true
                doConnect()
            }
        }
    }

    private fun postMain(block: () -> Unit) {
        mainHandler.post(block)
    }
}
