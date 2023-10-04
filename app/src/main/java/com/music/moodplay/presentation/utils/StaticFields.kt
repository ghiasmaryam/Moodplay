package com.music.moodplay.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import com.google.gson.Gson
import com.music.moodplay.AppController
import com.music.moodplay.models.localModel.SongsModel

object StaticFields {

    const val pulseRateKey: String = "PulseRateKey"
    const val fsPulseRatesKey: String = "FSPulseRateKey"
    const val songModelKey: String = "SongModelKey"
    const val songModelListKey: String = "SongModelListKey"
    const val songPositionKey: String = "SongPositionKey"
    const val favouriteSongListKey: String = "favouriteSongKey"
    const val isFavourite: String = "isFavourite"
    const val isEnglish: String = "isEnglish"
    const val deviceId: String = "DeviceId"
    var songsList: ArrayList<SongsModel>? = null

    const val MESSAGE_CAMERA_NOT_AVAILABLE = 3
    const val MESSAGE_UPDATE_REALTIME = 1
    const val MESSAGE_UPDATE_FINAL = 2

    const val actionPlay = "Action_Play"
    const val actionPause = "Action_Pause"
    const val actionNext = "Action_Next"
    const val actionPrevious = "Action_Previous"

    fun toastClass(text: String, context: Context = AppController.getContext()) {
        val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
//        toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0)
        toast.show()
    }

    private val gson = Gson()

    fun <T> modelToString(model: T): String {
        return gson.toJson(model)
    }

    fun <T> stringToModel(str: String, model: T): T {
        return gson.fromJson(str, model!!::class.java)
    }



}