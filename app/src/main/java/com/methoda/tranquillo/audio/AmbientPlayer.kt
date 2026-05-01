package com.methoda.tranquillo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.methoda.tranquillo.R

/**
 * Tiny ambient-sound engine. Wraps [MediaPlayer] to loop a single bundled
 * `res/raw/ambient_N.mp3` track at low volume; switching the track restarts
 * playback. Stopping releases the underlying player.
 *
 * Owned by [com.methoda.tranquillo.PerfectlyTranquilloApp] so a single
 * instance survives ViewModel re-creations.
 */
class AmbientPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var currentTrack: String? = null

    /**
     * Apply (sound on/off) + (chosen track id). If sound is off OR the id is
     * "none" or unknown, ambient stops. Otherwise the matching loop starts.
     * Idempotent — calling with the same args is a no-op.
     */
    fun apply(soundEnabled: Boolean, trackId: String) {
        val targetRes = if (!soundEnabled) null else trackId.toResId()
        val targetKey = if (targetRes == null) null else trackId

        if (targetKey == currentTrack) return

        stopInternal()
        currentTrack = targetKey
        if (targetRes != null) {
            startInternal(targetRes)
        }
    }

    fun stop() {
        stopInternal()
        currentTrack = null
    }

    private fun startInternal(rawResId: Int) {
        runCatching {
            val afd = context.resources.openRawResourceFd(rawResId) ?: return
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.isLooping = true
            mp.setVolume(LOOP_VOLUME, LOOP_VOLUME)
            mp.prepare()
            mp.start()
            player = mp
        }
    }

    private fun stopInternal() {
        player?.let {
            runCatching { if (it.isPlaying) it.stop() }
            runCatching { it.release() }
        }
        player = null
    }

    companion object {
        // Soft background volume — never fights with the user's other audio.
        private const val LOOP_VOLUME = 0.4f

        private fun String.toResId(): Int? = when (this) {
            "ambient_1" -> R.raw.ambient_1
            "ambient_2" -> R.raw.ambient_2
            "ambient_3" -> R.raw.ambient_3
            "ambient_4" -> R.raw.ambient_4
            "ambient_5" -> R.raw.ambient_5
            else        -> null
        }
    }
}
