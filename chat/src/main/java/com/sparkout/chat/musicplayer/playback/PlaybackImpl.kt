package com.sparkout.chat.musicplayer.playback

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


internal class PlaybackImpl(
    context: Context,
    audioManager: AudioManager,
    wifiLock: WifiManager.WifiLock
) : BasePlayback(context, audioManager, wifiLock), Player.EventListener {

    private var player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
        DefaultRenderersFactory(context), DefaultTrackSelector(), DefaultLoadControl()
    ).apply {
        audioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    override val isPlaying: Boolean
        get() = player.playWhenReady
    override val position: Long
        get() = player.currentPosition
    override val duration: Long
        get() = player.duration

    override fun startPlayer() {
        player
        play()
    }

    override fun pausePlayer() {
        player.playWhenReady = false
    }

    override fun stopPlayer() {
        player.release()
        player.removeListener(this)
        player.playWhenReady = false
    }

    override fun resumePlayer() {
        player.playWhenReady = true
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        TODO("Not yet implemented")
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> playbackCallback?.onIdle()
            Player.STATE_READY -> if (playWhenReady) playbackCallback?.onPlay() else playbackCallback?.onPause()
            Player.STATE_BUFFERING -> if (playWhenReady) playbackCallback?.onBuffer() else playbackCallback?.onPause()
            Player.STATE_ENDED -> playbackCallback?.onCompletion()
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        TODO("Not yet implemented")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        TODO("Not yet implemented")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        TODO("Not yet implemented")
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        TODO("Not yet implemented")
    }

    override fun onSeekProcessed() {
        TODO("Not yet implemented")
    }

    private fun play() {
        val dataSourceFactory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, "beeBush"), null
        )
        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(Uri.parse(currentMedia?.streamUrl))
        player.addListener(this)
        player.prepare(mediaSource)
        player.playWhenReady = true
    }
}