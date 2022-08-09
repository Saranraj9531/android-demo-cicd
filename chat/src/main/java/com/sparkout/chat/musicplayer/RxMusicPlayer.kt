package com.sparkout.chat.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.exoplayer2.offline.DownloadService.startForeground
import com.sparkout.chat.musicplayer.common.Action
import com.sparkout.chat.musicplayer.common.PlaybackState
import com.sparkout.chat.musicplayer.common.QueueData
import com.sparkout.chat.musicplayer.media.MediaService
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

object RxMusicPlayer {
    private val playbackStateSubject = BehaviorSubject.create<PlaybackState>()
    private val queueSubject = BehaviorSubject.create<QueueData>()
    private val actionSubject = PublishSubject.create<Action>()
    private val playbackPositionSubject = PublishSubject.create<Long>()

    @JvmStatic
    fun start(context: Context, intent: Intent? = null) {
        context.startService(Intent(context, MediaService::class.java).apply {
            putExtra(MediaService.EXTRA_INTENT, intent)
        })
    }

    @JvmStatic
    fun stop(context: Context) {
        context.stopService(Intent(context, MediaService::class.java))
    }

    @JvmStatic
    val state: BehaviorSubject<PlaybackState>
        get() = playbackStateSubject

    @JvmStatic
    val queue: BehaviorSubject<QueueData>
        get() = queueSubject

    @JvmStatic
    val action: PublishSubject<Action>
        get() = actionSubject

    @JvmStatic
    val position: PublishSubject<Long>
        get() = playbackPositionSubject
}