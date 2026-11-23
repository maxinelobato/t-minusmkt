package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onDoneListener: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale("pt", "BR")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onDoneListener?.invoke()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onDoneListener?.invoke()
                    }
                })
            }
        }
    }

    fun speak(text: String, isPortuguese: Boolean, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) {
            onDone?.invoke()
            return
        }

        onDoneListener = onDone
        tts?.language = if (isPortuguese) Locale("pt", "BR") else Locale.US
        // Clean out any task metadata tags before reading aloud
        val cleanText = text.replace(Regex("\\[TASK:[^\\]]+\\]"), "").trim()
        if (cleanText.isNotBlank()) {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "t_minus_ai_speech")
        } else {
            onDone?.invoke()
        }
    }

    fun stop() {
        tts?.stop()
        onDoneListener?.invoke()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
