package com.cathub.voice

import android.content.Context
import android.util.Log
import ai.picovoice.porcupine.Porcupine

/**
 * Porcupine wake word detection — "Jarvis".
 * Uses v1.9.5 (free, no access key needed for built-in keywords).
 */
object PorcupineWakeWord {
    private const val TAG = "PorcupineWakeWord"

    private var porcupine: Porcupine? = null
    private var _isInitialized = false

    val isInitialized: Boolean get() = _isInitialized
    val frameLength: Int get() = porcupine?.frameLength ?: 512
    val sampleRate: Int get() = porcupine?.sampleRate ?: 16000

    fun init(context: Context): Boolean {
        if (_isInitialized) return true
        return try {
            porcupine = Porcupine.Builder()
                .setKeyword(Porcupine.BuiltInKeyword.JARVIS)
                .setSensitivity(1.0f)
                .build(context)
            _isInitialized = true
            Log.i(TAG, "Porcupine initialized — keyword=JARVIS, sensitivity=1.0")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Porcupine init failed: ${e.message}", e)
            _isInitialized = false
            false
        }
    }

    fun process(pcm: ShortArray): Boolean {
        if (!_isInitialized || porcupine == null) return false
        return try {
            porcupine!!.process(pcm) >= 0
        } catch (e: Exception) {
            Log.e(TAG, "Porcupine process error: ${e.message}")
            false
        }
    }

    fun release() {
        try { porcupine?.delete() } catch (_: Exception) {}
        porcupine = null
        _isInitialized = false
    }
}
