package com.music.moodplay.models.firebaseModels

class FSSongsModel {
    var songName: String = ""
    var songURL: String = ""
    var english: Boolean = true
    var sad: Boolean = false
    var happy: Boolean = false
    var angry: Boolean = false
    var neutral: Boolean = false
    var key: String = ""              // the class stores a key property to track the song's unique identifier.
    var favoriteCount: Int = 0      //favoriteCount variable to keep track of how many times the song has been favorite.
}