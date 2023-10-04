package com.music.moodplay.presentation.utils.mediaInterfaces

import androidx.lifecycle.MutableLiveData

interface MediaChangeListener {
//    fun changeSong(position: Int)
    fun seekBarChange(progress: Int)
    fun getDuration(duration: Int)
//    fun getPausePlay(isPause: Boolean)
}

object MediaLiveData {
    var pausePlayLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var changeSongLiveData: MutableLiveData<Int> = MutableLiveData()
}