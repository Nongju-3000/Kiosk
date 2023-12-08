package com.wook.web.credo.kiosk.utils

import android.content.Context
import android.media.MediaPlayer
import com.wook.web.credo.kiosk.R

class MediaPlayerHandler {
    companion object {
        const val MORE_DEEP = 0
        const val MORE_FAST = 1
        const val TOO_DEEP = 2
        const val TOO_FAST = 3
        const val TRY_RELEASE = 4
        const val GOOD = 5
    }

    private var mediaPlayer = MediaPlayer()
    private var soundQueue = mutableListOf<Int>()
    private var prev_sound = -1
    private var count = 0

    fun playingSound(context: Context, sound: Int) {
        soundQueue.add(sound)
        if(!mediaPlayer.isPlaying) {
            val sounds = soundQueue.min()
            if(prev_sound != sounds || sounds == GOOD) {
                prev_sound = sounds
                playSound(context, sounds)
            } else {
                count++
                soundQueue.clear()
                if(count > 7) {
                    count = 0
                    prev_sound = -1
                    soundQueue.clear()
                }
            }
        }
    }

    private fun playSound(context: Context, sound: Int) {
        mediaPlayer.reset()
        when(sound) {
            0 -> mediaPlayer = MediaPlayer.create(context, R.raw.more_deep)
            1 -> mediaPlayer = MediaPlayer.create(context, R.raw.more_fast)
            2 -> mediaPlayer = MediaPlayer.create(context, R.raw.too_deep)
            3 -> mediaPlayer = MediaPlayer.create(context, R.raw.too_fast)
            4 -> mediaPlayer = MediaPlayer.create(context, R.raw.try_release)
            5 -> mediaPlayer = MediaPlayer.create(context, R.raw.good)
        }
        mediaPlayer.isLooping = false
        soundQueue.clear()
        mediaPlayer.start()
    }
}