package com.example.sololeveling90days.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale

object SoundManager {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGen: ToneGenerator? = null

    fun init(context: Context) {
        if (toneGen == null) {
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let {
                        val result = it.setLanguage(Locale.US)
                        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                            isTtsReady = true
                            it.setPitch(0.80f) // Deep, resonant robotic voice
                            it.setSpeechRate(0.95f) // Methodical pacing
                        }
                    }
                }
            }
        }
    }

    fun playQuestDone() {
        toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    fun playLevelUp() {
        Thread {
            try {
                // Play ascending tone arpeggio
                toneGen?.startTone(ToneGenerator.TONE_DTMF_1, 100)
                Thread.sleep(120)
                toneGen?.startTone(ToneGenerator.TONE_DTMF_5, 100)
                Thread.sleep(120)
                toneGen?.startTone(ToneGenerator.TONE_DTMF_9, 100)
                Thread.sleep(120)
                toneGen?.startTone(ToneGenerator.TONE_DTMF_D, 250)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playLockoutAlert() {
        Thread {
            try {
                for (i in 0..2) {
                    toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
                    Thread.sleep(450)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Announcer")
        }
    }

    fun speakQueue(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "AnnouncerQueue")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
        toneGen?.release()
        toneGen = null
    }
}
