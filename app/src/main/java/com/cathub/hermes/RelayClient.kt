package com.cathub.hermes
import com.cathub.voice.AudioPlayer

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString

/**
 * Simplified WebSocket client for Cat Hub → Hermes relay.
 *
 * Handles:
 * - Voice: PCM audio streaming (binary frames) + voice_start/voice_stop
 * - Responses: transcript, processing, response text
 * - TTS: binary audio frames from relay → AudioPlayer
 */
object RelayClient {
    private const val TAG = "RelayClient"
    private const val DEFAULT_SERVER_URL = "ws://100.111.44.87:8766"
    private const val DEFAULT_TOKEN = "hermes"
    private const val MAX_BACKOFF_MS = 30_000L
    private const val MAX_RETRIES = 20

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

    // ── Callbacks ──────────────────────────────────────────────
    var onConnectionChanged: ((Boolean, String) -> Unit)? = null
    var onResponseText: ((String) -> Unit)? = null
    var onSpeakingChanged: ((Boolean) -> Unit)? = null
    var onProcessing: (() -> Unit)? = null
    var onTranscript: ((String, Boolean) -> Unit)? = null

    // ── Public API ─────────────────────────────────────────────

    fun connect() {
        if (isConnected || isConnecting) return
        shouldReconnect = true
        isConnecting = true
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        doConnect()
    }

    fun disconnect() {
        shouldReconnect = false
        isConnecting = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        scope?.cancel()
        scope = null
        isConnected = false
        notifyStatus(false, "Disconnected")
    }

    fun sendAudio(data: ByteArray): Boolean {
        val ws = webSocket ?: return false
        if (!isConnected) return false
        return try {
            ws.send(ByteString.of(*data))
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send audio: ${e.message}")
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

    // ── Internal ───────────────────────────────────────────────

    private fun sendJson(json: String) {
        try { webSocket?.send(json) } catch (_: Exception) {}
    }

    private fun doConnect() {
        val wsUrl = "$DEFAULT_SERVER_URL/ws?token=$DEFAULT_TOKEN&device_id=cat-hub&model=FireHD10"
        Log.i(TAG, "Connecting to $wsUrl")
        notifyStatus(false, "Connecting...")

        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "Connected")
                isConnected = true
                isConnecting = false
                retryCount = 0
                notifyStatus(true, "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope?.launch { handleMessage(text) }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Binary = TTS audio from relay
                scope?.launch {
                    try {
                        AudioPlayer.play(bytes.toByteArray())
                        postMain { onSpeakingChanged?.invoke(true) }
                    } catch (e: Exception) {
                        Log.w(TAG, "Audio playback error: ${e.message}")
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "Closed: $code $reason")
                isConnected = false
                isConnecting = false
                notifyStatus(false, "Disconnected")
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Connection failed: ${t.message}")
                isConnected = false
                isConnecting = false
                notifyStatus(false, "Connection lost")
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
                "notification" -> {
                    val title = json.get("title")?.asString ?: "Hermes"
                    val body = json.get("body")?.asString ?: ""
                    Log.i(TAG, "Notification: $title — $body")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Message parse error: ${e.message}")
        }
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect || retryCount >= MAX_RETRIES) return
        retryCount++
        val delay = minOf(1000L * (1 shl retryCount), MAX_BACKOFF_MS)
        Log.i(TAG, "Reconnecting in ${delay}ms (attempt $retryCount)")
        reconnectJob = scope?.launch {
            delay(delay)
            if (shouldReconnect && !isConnected) {
                isConnecting = true
                doConnect()
            }
        }
    }

    private fun notifyStatus(connected: Boolean, msg: String) {
        postMain { onConnectionChanged?.invoke(connected, msg) }
    }

    private fun postMain(block: () -> Unit) {
        mainHandler.post(block)
    }
}
