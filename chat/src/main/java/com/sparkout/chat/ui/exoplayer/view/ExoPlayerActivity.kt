package com.sparkout.chat.ui.exoplayer.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.showToast
import com.sparkout.chat.databinding.ActivityExoPlayerBinding
import timber.log.Timber


class ExoPlayerActivity : AppCompatActivity() {
    var mUri: String? = null
    var simpleExoPlayer: SimpleExoPlayer? = null
    private var mEventListener: Player.EventListener? = null
    private var playWhenReady = false
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private lateinit var binding: ActivityExoPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageviewBack.setOnClickListener {
            finish()
        }

        mUri = intent.getStringExtra("URL")
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource(
            uri,
            DefaultDataSourceFactory(this@ExoPlayerActivity, "ua"),
            DefaultExtractorsFactory(), null, null
        )
    }

    private fun initializePlayer() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this@ExoPlayerActivity),
            DefaultTrackSelector(),
            DefaultLoadControl()
        )
        binding.playerView.player = simpleExoPlayer
        //        simpleExoPlayer.setPlayWhenReady(playWhenReady);
        simpleExoPlayer!!.playWhenReady = true
        simpleExoPlayer!!.seekTo(currentWindow, playbackPosition)
        if (mUri != null) {
            val uri = Uri.parse(mUri)
            val mediaSource = buildMediaSource(uri)
            simpleExoPlayer!!.prepare(mediaSource, true, false)
            try {
                mEventListener = object : Player.EventListener {
                    override fun onTimelineChanged(
                        timeline: Timeline?,
                        manifest: Any?,
                        reason: Int
                    ) {
                        
                    }

                    override fun onTracksChanged(
                        trackGroups: TrackGroupArray,
                        trackSelections: TrackSelectionArray
                    ) {
                    }

                    override fun onLoadingChanged(isLoading: Boolean) {
                        if (isLoading) {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }

                    override fun onPlayerStateChanged(
                        playWhenReady: Boolean,
                        playbackState: Int
                    ) {
                        if (playbackState == Player.STATE_READY) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {}
                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

                    override fun onPlayerError(error: ExoPlaybackException) {}
                    override fun onPositionDiscontinuity(reason: Int) {}
                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
                    override fun onSeekProcessed() {}
                }
            } catch (e: Exception) {
                Timber.tag("Krish").e("Exo_listener: ${e.message}")
            }
            simpleExoPlayer!!.addListener(mEventListener!!)
        } else {
            showToast(this, "Unable to play")
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || simpleExoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null && mEventListener != null) {
            simpleExoPlayer!!.removeListener(mEventListener!!)
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            playWhenReady = simpleExoPlayer!!.playWhenReady
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }
}
