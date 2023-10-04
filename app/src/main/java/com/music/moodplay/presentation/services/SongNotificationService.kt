package com.music.moodplay.presentation.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.music.moodplay.R
import com.music.moodplay.models.localModel.SongsModel
import com.music.moodplay.presentation.utils.StaticFields

class SongNotificationService {

    private val notificationId: Int = 1

    fun initNotification(
        playbackStatus: PlaybackStatus, context: Context,
        songsModel: SongsModel, mediaSessionCompat: MediaSessionCompat
    ) {
        val notificationIcon: Int

        var pendingIntent: PendingIntent? = null

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationIcon = R.drawable.ic_baseline_pause_24
            pendingIntent = playbackAction(1, context)
        } else {
            notificationIcon = R.drawable.ic_baseline_play_arrow_24
            pendingIntent = playbackAction(0, context)
        }

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationId.toString(),
                "Default channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, notificationId.toString())
                .setShowWhen(false) // Set the Notification style
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle() // Attach our MediaSession token
                        .setMediaSession(mediaSessionCompat.sessionToken) // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2)
                ) // Set the Notification color
                .setColor(
                    ContextCompat.getColor(
                        context,
                        R.color.purple_500
                    )
                ) // Set the large and small icons
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo))
                .setSmallIcon(android.R.drawable.stat_sys_headset) // Set Notification content information
                .setContentTitle(songsModel.songName) // Add playback actions
                .addAction(
                    android.R.drawable.ic_media_previous,
                    "previous",
                    playbackAction(3, context)
                )
                .addAction(notificationIcon, "pause", pendingIntent)
                .addAction(
                    android.R.drawable.ic_media_next,
                    "next",
                    playbackAction(2, context)
                )

        mNotificationManager.notify(
            notificationId,
            notificationBuilder.build()
        )
    }

    enum class PlaybackStatus {
        PLAYING, PAUSED
    }

    private fun playbackAction(actionNumber: Int, context: Context): PendingIntent? {
        val playbackAction = Intent(context, MusicPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = StaticFields.actionPlay

                return PendingIntent.getService(
                    context, actionNumber, playbackAction,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            1 -> {
                // Pause
                playbackAction.action = StaticFields.actionPause
                return PendingIntent.getService(
                    context, actionNumber, playbackAction,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            2 -> {
                // Next track
                playbackAction.action = StaticFields.actionNext
                return PendingIntent.getService(
                    context, actionNumber, playbackAction,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            3 -> {
                // Previous track
                playbackAction.action = StaticFields.actionPrevious
                return PendingIntent.getService(
                    context, actionNumber, playbackAction,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            else -> {}
        }
        return null
    }

    fun removeNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.cancel(notificationId)
    }

}