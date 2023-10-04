package com.music.moodplay.presentation.utils

interface PulseRateHandler {
    fun updatingData(code: Int, message: String, data: Float)
}