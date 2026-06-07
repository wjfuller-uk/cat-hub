package com.cathub.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.cathub.MainActivity
import com.cathub.hermes.RelayClient
import kotlinx.coroutines.*

/**
 * Voice service — wake word detection + audio streaming.
 *
 * State machine: IDLE → STREAMING → IDLE
 * Only ONE coroutine reads AudioRecord at a time.
 */
class VoiceService : Service() {
    companion object {
        private const val TAG = "VoiceService"
        private const val CHANNEL_ID = "cat_hub_voice"
        private const val NOTIFICATION_ID = 3001
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        enum class State { IDLE, STREAMING }

        @Volatile
        var currentState = State.IDLE
            private set

        var onWakeWord: (() -> Unit)? = null
        var onTranscript: ((String, Boolean) -> Unit)? = null
        var onResponseText: ((String) -> Unit)? = null
        var onSpeakingChanged: ((Boolean) -> Unit)? = null
        var onConnectionChanged: ((Boolean, String) -> Unit)? = null
    }

    private var audioRecord: AudioRecord? = null
    private var activeJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cathub:voice").apply {
            acquire(60 * 60 * 1000L)
        }

        // Initialize Porcupine
        PorcupineWakeWord.init(this)
        Log.i(TAG, "VoiceService created, porcupine=${PorcupineWakeWord.isInitialized}")

        // Wire up relay callbacks
        RelayClient.onConnectionChanged = { connected, msg ->
            onConnectionChanged?.invoke(connected, msg)
        }
        RelayClient.onTranscript = { text, isFinal ->
            onTranscript?.invoke(text, isFinal)
        }
        RelayClient.onResponseText = { text ->
            onResponseText?.invoke(text)
        }
        RelayClient.onSpeakingChanged = { speaking ->
            onSpeakingChanged?.invoke(speaking)
        }
        RelayClient.onProcessing = {}
        RelayClient.connect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(NOTIFICATION_ID, buildNotification())
        } catch (e: RuntimeException) {
            Log.e(TAG, "Foreground not allowed", e)
            stopSelf()
            return START_NOT_STICKY
        }
        startWakeWordLoop()
        return START_STICKY
    }

    override fun onDestroy() {
        currentState = State.IDLE
        activeJob?.cancel()
        try { audioRecord?.stop() } catch (_: Exception) {}
        audioRecord?.release()
        audioRecord = null
        PorcupineWakeWord.release()
        AudioPlayer.release()
        scope.cancel()
        wakeLock?.let { if (it.isHeld) it.release() }
        RelayClient.disconnect()
        Log.i(TAG, "VoiceService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── IDLE state: wake word detection ────────────────────────

    private fun startWakeWordLoop() {
        currentState = State.IDLE
        ensureAudioRecord()
        audioRecord?.startRecording()
        Log.i(TAG, "Wake word loop started (IDLE)")

        activeJob = scope.launch {
            val frameLen = PorcupineWakeWord.frameLength
            val buffer = ShortArray(frameLen)

            while (isActive && currentState == State.IDLE) {
                try {
                    val shortsRead = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (shortsRead != frameLen) continue

                    if (PorcupineWakeWord.process(buffer)) {
                        Log.i(TAG, "🎤 Wake word detected!")
                        postMain { onWakeWord?.invoke() }
                        transitionToStreaming()
                        return@launch // Exit wake word loop — streaming owns AudioRecord now
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Wake word error: ${e.message}")
                }
            }
        }
    }

    // ── STREAMING state: send PCM to relay ─────────────────────

    private fun transitionToStreaming() {
        currentState = State.STREAMING
        RelayClient.sendVoiceStart()

        activeJob = scope.launch {
            val frameMs = 40
            val frameSamples = SAMPLE_RATE * frameMs / 1000
            val frameBytes = frameSamples * 2
            val buffer = ByteArray(frameBytes)
            var silenceFrames = 0
            val maxSilenceFrames = 75 // ~3 seconds silence

            Log.i(TAG, "Streaming started")

            while (isActive && currentState == State.STREAMING) {
                try {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (bytesRead <= 0) continue

                    RelayClient.sendAudio(buffer.copyOf(bytesRead))

                    // RMS silence detection
                    var sum = 0L
                    for (i in 0 until bytesRead step 2) {
                        val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                        sum += sample.toShort().toLong() * sample.toShort()
                    }
                    val rms = kotlin.math.sqrt(sum.toDouble() / (bytesRead / 2))

                    if (rms < 300) {
                        silenceFrames++
                        if (silenceFrames >= maxSilenceFrames) {
                            Log.i(TAG, "Silence timeout — stopping stream")
                            break
                        }
                    } else {
                        silenceFrames = 0
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Streaming error: ${e.message}")
                    break
                }
            }

            RelayClient.sendVoiceStop()
            Log.i(TAG, "Streaming stopped → returning to IDLE")

            // Transition back to IDLE — restart wake word loop
            postMain { onSpeakingChanged?.invoke(false) }
            startWakeWordLoop()
        }
    }

    // ── AudioRecord setup ──────────────────────────────────────

    private fun ensureAudioRecord() {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) return
        try { audioRecord?.release() } catch (_: Exception) {}

        val sources = intArrayOf(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.DEFAULT,
        )

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufSize = maxOf(minBuf, 4096)

        for (src in sources) {
            try {
                audioRecord = AudioRecord(src, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufSize)
                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    Log.i(TAG, "AudioRecord OK: src=$src")
                    return
                }
                audioRecord?.release()
            } catch (_: Exception) {}
        }
        Log.e(TAG, "AudioRecord init failed")
        audioRecord = null
    }

    // ── Helpers ────────────────────────────────────────────────

    private fun postMain(block: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(block)
    }

    // ── Notification ───────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Cat Hub Voice",
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = "Wake word detection" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("🐱 Cat Hub")
            .setContentText("Listening for \"Jarvis\"...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }
}
