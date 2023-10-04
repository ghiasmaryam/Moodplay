package com.music.moodplay.presentation.services

import android.support.v4.media.session.MediaSessionCompat
import android.util.Log

class MediaSessionCompatCallback: MediaSessionCompat.Callback() {

    override fun onSkipToNext() {
        super.onSkipToNext()
        Log.d("CallbackFunctions", "SkipToNext")
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        Log.d("CallbackFunctions", "SkipToPrevious")
    }

    override fun onPause() {
        super.onPause()
        Log.d("CallbackFunctions", "SkipToPause")
    }

    override fun onPlay() {
        super.onPlay()
        Log.d("CallbackFunctions", "SkipToPlay")
    }

}