package com.music.moodplay.models.localModel

data class SongsModel(
    var songName: String = "",
    var songURL: String = "",
    var favoriteCount: Int = 0,
    var key: String = ""
)

//Data classes in Kotlin are special classes primarily used to hold data