package com.music.moodplay.presentation.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.music.moodplay.AppController
import com.music.moodplay.R
import com.music.moodplay.models.localModel.SongsModel
import com.music.moodplay.presentation.utils.StaticFields
import com.music.moodplay.presentation.utils.mediaInterfaces.MediaChangeListener
import com.music.moodplay.presentation.utils.mediaInterfaces.MediaLiveData

class MusicPlayerService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnSeekCompleteListener {

    private var mediaPlayer: MediaPlayer? = null  //: is use for instance
    lateinit var myServiceBinder: MyServiceBinder
    private var resumePosition: Int = 0// An integer to store the position of the audio playback when it is paused and resumed
    private var songPosition: Int = 0//An integer to keep track of the current position of the song in the
    private var songsList: ArrayList<SongsModel>? = ArrayList()
    private var isFavorite: Boolean = false
    private var isEnglish: Boolean = false
    private var mediaChangeListener: MediaChangeListener? = null

    private var mediaSession: MediaSessionManager? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private lateinit var songNotificationService: SongNotificationService

    public fun getIsEnglish() :Boolean? {
        return if (isFavorite) null else isEnglish
    }

         //when services is started onstart is called
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == null) {
                songPosition = it.getIntExtra(StaticFields.songPositionKey, 0)
                songsList = StaticFields.songsList
                isEnglish = it.getBooleanExtra(StaticFields.isEnglish, false)
                songsList?.let {
                    initMediaPlayer()
                }
            }
            handleIncomingNotificationActions(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myServiceBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        songNotificationService.removeNotification(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        myServiceBinder = MyServiceBinder()
        mediaSession = AppController.getContext()
            .getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSessionCompat = MediaSessionCompat(AppController.getContext(), "AudioPlayer")
        mediaSessionCompat?.let {
            it.isActive = true
            it.setCallback(MediaSessionCompatCallback())
        }
        songNotificationService = SongNotificationService()
    }

    private fun initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.let {
                it.setOnPreparedListener(this)
                it.setOnErrorListener(this)
                it.setOnCompletionListener(this)
                it.setOnSeekCompleteListener(this)
                startSong()
            }
        }
    }

    private fun startSong() {
        songsList?.let { songs ->
            mediaPlayer?.setDataSource(songs[songPosition].songURL)
            mediaPlayer?.prepare()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        playSongs()
    }

    fun updateData(
        position: Int,
        isFavorite: Boolean,
        isEnglish: Boolean,
        mediaChangeListener: MediaChangeListener
    ) {
        songsList = StaticFields.songsList
        this.mediaChangeListener = mediaChangeListener
        if (position != songPosition || this.isFavorite != isFavorite || this.isEnglish != isEnglish) {
            if (position < 0 || position == songsList!!.size) {
                return
            }
            songPosition = position
            startNext()
        } else {
            this.mediaChangeListener?.getDuration(mediaPlayer?.duration!!)
        }
        this.isEnglish = isEnglish
        this.isFavorite = isFavorite
    }

    private fun startNext() {
        songsList?.let {
            stopSongs()
            mediaPlayer?.reset()
            startSong()
        }
    }

    private fun playSongs() {
        mediaPlayer?.let {
            if (!it.isPlaying)
                it.start()
            mediaChangeListener?.getDuration(it.duration)
            initThread()
            setUpMediaData()
//            mediaChangeListener?.changeSong(songPosition)
            MediaLiveData.changeSongLiveData.postValue(songPosition)
            songNotificationService.initNotification(
                SongNotificationService.PlaybackStatus.PLAYING,
                applicationContext, songsModel = songsList!![songPosition], mediaSessionCompat!!
            )
        }
    }

    private fun setUpMediaData() {
        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        mediaSessionCompat?.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    songsList!![songPosition].songName
                )
                .build()
        )
    }

    private lateinit var runnable: Runnable
    //initThread runs on a separate thread to periodically update the progress of the currently playing song.
    private fun initThread() {
        val handlerThread = Handler(mainLooper)
        runnable = Runnable {
            mediaPlayer?.let {
                mediaChangeListener?.seekBarChange(it.currentPosition)
            }
            handlerThread.postDelayed(runnable, 1000)
        }
        handlerThread.post(runnable)
    }

    fun pauseResumeSongs() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
//                mediaChangeListener?.getPausePlay(true)
                MediaLiveData.pausePlayLiveData.postValue(true)
                songNotificationService.initNotification(
                    SongNotificationService.PlaybackStatus.PAUSED,
                    applicationContext, songsModel = songsList!![songPosition], mediaSessionCompat!!
                )
            } else {
                it.seekTo(resumePosition)
                it.start()
                MediaLiveData.pausePlayLiveData.postValue(false)
//                mediaChangeListener?.getPausePlay(false)
                songNotificationService.initNotification(
                    SongNotificationService.PlaybackStatus.PLAYING,
                    applicationContext, songsModel = songsList!![songPosition], mediaSessionCompat!!
                )
            }
        }

    }

    private fun stopSongs() {
        mediaPlayer?.let {
            resumePosition = 0
            it.stop()
        }
    }

    inner class MyServiceBinder : Binder() {

        fun getService(mediaChangeListener: MediaChangeListener): MusicPlayerService {
            this@MusicPlayerService.mediaChangeListener = mediaChangeListener
            mediaPlayer?.let {
                this@MusicPlayerService.mediaChangeListener?.getDuration(it.duration)
            }
            return this@MusicPlayerService
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (songPosition < (songsList?.size!! - 1)) {
            songPosition++
            startNext()
        }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        Log.d("SongcompletedSeek", "afdsa")
    }

    fun updateSongProgress(progress: Int) {
        mediaPlayer?.seekTo(progress)
    }

    private fun handleIncomingNotificationActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        when (playbackAction.action) {
            StaticFields.actionPlay -> {
                pauseResumeSongs()
            }
            StaticFields.actionPause -> {
                pauseResumeSongs()
            }
            StaticFields.actionNext -> {
                updateData(
                    position = songPosition + 1,
                    this.isFavorite,
                    this.isEnglish,
                    this.mediaChangeListener!!
                )
            }
            StaticFields.actionPrevious -> {
                updateData(
                    position = songPosition - 1,
                    this.isFavorite,
                    this.isEnglish,
                    this.mediaChangeListener!!
                )
            }
        }
    }

}