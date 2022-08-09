package com.sparkout.chat.musicplayer.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sparkout.chat.R
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.musicplayer.common.PlaybackState
import com.sparkout.chat.musicplayer.media.Media
import com.sparkout.chat.musicplayer.media.MediaService

internal class NotificationManager(
    private val service: Service,
    private val token: MediaSessionCompat.Token,
    private val notificationManager: NotificationManagerCompat
) {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val PLAYER_PENDING_INTENT_ID = 10
        private const val PAUSE_PENDING_INTENT_ID = 20
        private const val PLAY_PENDING_INTENT_ID = 30
        private const val PLAY_NEXT_PENDING_INTENT_ID = 40
        private const val PLAY_PREV_PENDING_INTENT_ID = 50
        private const val STOP_PENDING_INTENT_ID = 60
    }

    private lateinit var notificationChannels: NotificationChannels
    private var intent: Intent? = null

    @Volatile
    private var hasStarted: Boolean = false
    private var handler = Handler()
    private var state: PlaybackState = PlaybackState.Idle
    private var media: Media? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannels = NotificationChannels(service)
        }
    }

    fun setIntent(intent: Intent?) {
        this.intent = intent
    }

    fun updateMedia(media: Media?) {
        this.media = media
    }

    fun updateState(state: PlaybackState) {
        this.state = state
        when (state) {
            is PlaybackState.Buffering -> updateNotification()
            is PlaybackState.Playing -> updateNotification()
            is PlaybackState.Paused -> pauseNotification()
            is PlaybackState.Completed -> pauseNotification()
            else -> stopNotification()
        }
    }

    private fun updateNotification() {
        Log.e("Nive ", "updateNotification: $hasStarted")
        if (!hasStarted) {
            val intent = Intent(service, MediaService::class.java)
            ContextCompat.startForegroundService(service, intent)
        }
        startNotification()
    }

    private fun pauseNotification() {
        startNotification()
        handler.postDelayed({
            service.stopForeground(false)
            hasStarted = false
        }, 100)
    }

    private fun stopNotification() {
        hasStarted = false
        media = null
        notificationManager.cancel(NOTIFICATION_ID)
        service.stopSelf()
    }

    private fun pause(context: Context): NotificationCompat.Action {
        val pendingIntent = getPendingIntent(
            context,
            PAUSE_PENDING_INTENT_ID,
            MediaService.ACTION_PAUSE
        )
        return NotificationCompat.Action(R.drawable.ic_audio_pause, "Pause", pendingIntent)
    }

    /*  private fun next(context: Context): NotificationCompat.Action {
          val pendingIntent = getPendingIntent(
              context,
              PLAY_NEXT_PENDING_INTENT_ID,
              MediaService.ACTION_NEXT
          )
          return NotificationCompat.Action(R.drawable.ic_music_player_next, "Next", pendingIntent)
      }

      private fun prev(context: Context): NotificationCompat.Action {
          val pendingIntent = getPendingIntent(
              context,
              PLAY_PREV_PENDING_INTENT_ID,
              MediaService.ACTION_PREV
          )
          return NotificationCompat.Action(R.drawable.ic_music_player_prev, "Previous", pendingIntent)
      }*/
    private fun play(context: Context): NotificationCompat.Action {
        val pendingIntent = getPendingIntent(
            context,
            PLAY_PENDING_INTENT_ID,
            MediaService.ACTION_PLAY
        )
        return NotificationCompat.Action(R.drawable.ic_audio_play, "Start", pendingIntent)
    }

    private fun dismiss(context: Context): PendingIntent {
        return getPendingIntent(
            context,
            STOP_PENDING_INTENT_ID,
            MediaService.ACTION_STOP
        )
    }

    private fun getPendingIntent(context: Context, intentId: Int, action: String): PendingIntent {
        val prevIntent = Intent(context, MediaService::class.java)
        prevIntent.action = action

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return PendingIntent.getService(
                context,
                intentId, prevIntent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            return PendingIntent.getService(
                context,
                intentId, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

        }

    }

    private fun startNotification() {
        val builder = createBuilder()
        Glide.with(service)
            .asBitmap()
            .load(R.drawable.ic_launcher)
            .apply(
                RequestOptions
                    .diskCacheStrategyOf(DiskCacheStrategy.DATA)
                    .onlyRetrieveFromCache(true)
                    .priority(Priority.IMMEDIATE)
            )
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    showNotification(builder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e("Nive ", "onLoadFailed:Notification ")
                    showNotification(builder)
                }
            })
    }

    private fun showNotification(builder: NotificationCompat.Builder) {
        if (media == null || state == PlaybackState.Idle) {
            return
        }

        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0)
        )
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(""))
            .setPriority(Notification.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setUsesChronometer(false)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_launcher)
            .setOnlyAlertOnce(true)
            .setContentText("Voice Message Audio")
            .setDeleteIntent(dismiss(service))

        if (state is PlaybackState.Paused || state is PlaybackState.Completed) {
            builder.addAction(play(service))
        } else {
            builder.addAction(pause(service))
        }
        builder.setLargeIcon(
            BitmapFactory.decodeResource(
                ChatApp.mInstance.resources,
                R.drawable.ic_launcher
            )
        )
        val notificationIntent = intent ?: Intent()
        builder.setContentIntent(
            PendingIntent.getActivity(
                service,
                PLAYER_PENDING_INTENT_ID, notificationIntent, 0
            )
        )
        notify(builder.build())
    }

    private fun notify(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.e("Nive ", "notify:Has $hasStarted")
        if (!hasStarted) {
            service.startForeground(NOTIFICATION_ID, notification)
            hasStarted = true
        }
    }

    private fun createBuilder(): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(service, notificationChannels.primaryChannel)
        } else {
            NotificationCompat.Builder(service)
        }
    }
}