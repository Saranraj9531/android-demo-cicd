package com.sparkout.chat.ui.audio.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sparkout.chat.R;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/***
 * A simple audio player wrapper for Android
 ***/
public class AudioWifeNew {

    private static final String TAG = AudioWifeNew.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static AudioWifeNew mAudioWife;

    private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;
    private Handler mProgressUpdateHandler;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private View mPlayButton;
    private View mPauseButton;
    private View mSlash;
    private TextView mRunTime;
    private TextView mTotalTime;


    private ArrayList<OnCompletionListener> mCompletionListeners = new ArrayList<OnCompletionListener>();

    private ArrayList<View.OnClickListener> mPlayListeners = new ArrayList<View.OnClickListener>();

    private ArrayList<View.OnClickListener> mPauseListeners = new ArrayList<View.OnClickListener>();


    private static Uri mUri;
    private Context mCtx;

    public static AudioWifeNew getInstance() {

        mAudioWife = new AudioWifeNew();

        return mAudioWife;
    }

    private Runnable mUpdateProgress = new Runnable() {

        public void run() {

            if (mSeekBar == null) {
                return;
            }

            if (mProgressUpdateHandler != null && mMediaPlayer.isPlaying()) {
                mSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
                int currentTime = mMediaPlayer.getCurrentPosition();
                updatePlaytime(currentTime);
                updateRuntime(currentTime);
                // repeat the process
                mProgressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
            } else {
                // DO NOT update UI if the player is paused
            }
        }
    };


    private void setViewsVisibility() {

        if (mSeekBar != null) {
            mSeekBar.setVisibility(View.VISIBLE);
        }

        if (mRunTime != null) {
            mRunTime.setVisibility(View.VISIBLE);
        }

        if (mTotalTime != null) {
            mTotalTime.setVisibility(View.VISIBLE);
        }

        if (mPlayButton != null) {
            mPlayButton.setVisibility(View.VISIBLE);
        }

        if (mPauseButton != null) {
            mPauseButton.setVisibility(View.VISIBLE);
        }

        if (mSlash != null) {
            mSlash.setVisibility(View.VISIBLE);
        }
    }

    @Deprecated
    private void updatePlaytime(int currentTime) {

        if (currentTime < 0) {
            throw new IllegalArgumentException(mCtx.getResources().getString(R.string.ERROR_PLAYTIME_CURRENT_NEGATIVE));
        }

        StringBuilder playbackStr = new StringBuilder();

        // set the current time
        // its ok to show 00:00 in the UI
        playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

        playbackStr.append("/");

        // show total duration.
        long totalDuration = 0;

        if (mMediaPlayer != null) {
            try {
                totalDuration = mMediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // set total time as the audio is being played
        if (totalDuration != 0) {
            playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
        } else {
            Timber.tag("Krish").e("ESomething strage this audio track duration in zero");
        }

        // DebugLog.i(currentTime + " / " + totalDuration);
    }

    private OnCompletionListener mOnCompletion = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // set UI when audio finished playing
            int currentPlayTime = 0;
            mSeekBar.setProgress((int) currentPlayTime);
            updatePlaytime(currentPlayTime);
            updateRuntime(currentPlayTime);
            setPlayable();
            // ensure that our completion listener fires first.
            // This will provide the developer to over-ride our
            // completion listener functionality

            fireCustomCompletionListeners(mp);
        }
    };

    private void fireCustomCompletionListeners(MediaPlayer mp) {
        for (OnCompletionListener listener : mCompletionListeners) {
            listener.onCompletion(mp);
        }
    }

    public AudioWifeNew initiateAll(Context ctx, Uri uri, View play, View pause, View slash, SeekBar seekbar, TextView currentTime, TextView totalTime) {
        release();
        if (ctx == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (uri == null) {
            throw new IllegalArgumentException(ctx.getResources().getString(R.string.ERROR_URI_NULL));
        }

        if (mAudioWife == null) {
            mAudioWife = new AudioWifeNew();
        }

        mUri = uri;

        mCtx = ctx;

        mProgressUpdateHandler = new Handler();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(ctx, mUri);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnCompletionListener(mOnCompletion);

        if (play == null) {
            throw new NullPointerException(mCtx.getResources().getString(R.string.ERROR_PLAYVIEW_NULL));
        }
        mPlayButton = play;


        mPlayListeners.add(0, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                play();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (View.OnClickListener listener : mPlayListeners) {
                    listener.onClick(v);
                }
            }
        });

        mSlash = slash;
        mPauseButton = pause;
        mPauseListeners.add(0, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pause();
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (View.OnClickListener listener : mPauseListeners) {
                    listener.onClick(v);
                }
            }
        });

        mSeekBar = seekbar;
        mSeekBar.setProgress(0);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaPlayer.seekTo(seekBar.getProgress());
                updateRuntime(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });

        mRunTime = currentTime;
        updateRuntime(0);

        mTotalTime = totalTime;

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                long finalTime = mp.getDuration();
                mSeekBar.setMax((int) finalTime);
                mTotalTime.setText(formateMilliSeccond(mMediaPlayer.getDuration()));
                play();
            }
        });

        return this;
    }

    private void updateRuntime(int currentTime) {

        if (mRunTime == null) {
            // this view can be null if the user
            // does not want to use it. Don't throw
            // an exception.
            return;
        }

        if (currentTime < 0) {
            throw new IllegalArgumentException(mCtx.getResources().getString(R.string.ERROR_PLAYTIME_CURRENT_NEGATIVE));
        }

        StringBuilder playbackStr = new StringBuilder();

        // set the current time
        // its ok to show 00:00 in the UI
        playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

        mRunTime.setText(playbackStr);

        // DebugLog.i(currentTime + " / " + totalDuration);
    }

    public void release() {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mProgressUpdateHandler = null;
        }
    }

    private void play() {

        // if play button itself is null, the whole purpose of AudioWifeNew is
        // defeated.
        if (mPlayButton == null) {
            throw new IllegalStateException(mCtx.getResources().getString(R.string.ERROR_PLAYVIEW_NULL));
        }

        if (mUri == null) {
            throw new IllegalStateException(mCtx.getResources().getString(R.string.URI_NULL));
        }

        if (mMediaPlayer == null) {
            throw new IllegalStateException(mCtx.getResources().getString(R.string.CALL_INIT));
        }

        if (mMediaPlayer.isPlaying()) {
            return;
        }

        mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);

        // enable visibility of all UI controls.
        setViewsVisibility();

        mMediaPlayer.start();

        setPausable();
    }

    private void pause() {

        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setPlayable();
        }
    }

    private void setPlayable() {
        if (mPlayButton != null) {
            mPlayButton.setVisibility(View.VISIBLE);
        }

        if (mPauseButton != null) {
            mPauseButton.setVisibility(View.GONE);
        }
    }

    private void setPausable() {
        if (mPlayButton != null) {
            mPlayButton.setVisibility(View.GONE);
        }

        if (mPauseButton != null) {
            mPauseButton.setVisibility(View.VISIBLE);
        }
    }

    private static String getDuration(Uri uri) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            File file = new File(uri.toString());
            mediaMetadataRetriever.setDataSource(file.getPath());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return formateMilliSeccond(Long.parseLong(durationStr));
        } catch (Exception e) {
            return null;
        }
    }

    private static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }
}
