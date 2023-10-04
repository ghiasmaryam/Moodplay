package com.music.moodplay.presentation.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.music.moodplay.databinding.ActivityMainBinding
import com.music.moodplay.presentation.services.MusicPlayerService
import com.music.moodplay.presentation.utils.InternetConLiveData
import com.music.moodplay.presentation.utils.StaticFields
import com.music.moodplay.presentation.utils.mediaInterfaces.MediaChangeListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var connection: InternetConLiveData
    private var musicPlayerService: MusicPlayerService? = null
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavGraph()
        checkNetworkConnection()
    }

    fun initService(
        position: Int,
        isFavorite: Boolean,
        isEnglish: Boolean,
        mediaChangeListener: MediaChangeListener
    ) {
        if (musicPlayerService == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder: MusicPlayerService.MyServiceBinder =
                        service as MusicPlayerService.MyServiceBinder
                    musicPlayerService = binder.getService(mediaChangeListener)
                }

                override fun onServiceDisconnected(name: ComponentName?) {

                }
            }
            val intentService = Intent(this, MusicPlayerService::class.java)
//        intentService.putExtra(StaticFields.songModelListKey, StaticFields.modelToString(songsList))

            intentService.putExtra(StaticFields.songPositionKey, position)
            intentService.putExtra(StaticFields.isEnglish, isEnglish)
            startService(intentService)
            bindService(intentService, serviceConnection, BIND_AUTO_CREATE)
        } else {
            musicPlayerService?.updateData(
                position,
                isFavorite,
                isEnglish = isEnglish,
                mediaChangeListener
            )
        }
    }

    fun changeSongProgress(progress: Int) {
        musicPlayerService?.updateSongProgress(progress)
    }

    private fun initNavGraph() {
        val navigator =
            supportFragmentManager.findFragmentById(binding.navGraphFragment.id) as NavHostFragment
        val navController = navigator.navController
    }

    private fun checkNetworkConnection() {

        connection = InternetConLiveData(this)

        connection.observe(this@MainActivity) { isConnected ->

            if (isConnected) {
                binding.noInt.noInternet.visibility = View.GONE

            } else {
                binding.noInt.noInternet.visibility = View.VISIBLE

            }
        }
    }

    fun pausePlaySong() {
        musicPlayerService?.pauseResumeSongs()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(serviceConnection)
        } catch (ex: Exception) {
        }
    }

    public fun getIsEnglishFromService() = musicPlayerService?.getIsEnglish()

}