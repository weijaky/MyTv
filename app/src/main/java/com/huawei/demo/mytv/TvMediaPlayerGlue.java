
package com.huawei.demo.mytv;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.media.PlaybackControlGlue;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.support.v17.leanback.media.SurfaceHolderGlueHost;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.IOException;
import java.util.List;

/**
 * This glue extends the {@link android.support.v17.leanback.media.PlaybackControlGlue} with a
 * {@link MediaPlayer} synchronization. It supports 7 actions:
 * <p>
 * <ul>
 * <li>{@link android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction}</li>
 * <li>{@link android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction}</li>
 * <li>{@link  android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction}</li>
 * <li>{@link android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction}</li>
 * <li>{@link android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction}</li>
 * <li>{@link android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction}</li>
 * </ul>
 *
 * @hide
 */
public class TvMediaPlayerGlue extends PlaybackControlGlue implements
        OnItemViewSelectedListener {

    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;

    private static final long MEDIA_SESSION_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PREPARE
                    | PlaybackStateCompat.ACTION_SEEK_TO;

    public static final int FAST_FORWARD_REWIND_STEP = 10 * 1000; // in milliseconds
    public static final int FAST_FORWARD_REWIND_REPEAT_DELAY = 200; // in milliseconds
    private static final String TAG = "MediaPlayerGlue";
    protected final PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    protected final PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    protected final PlaybackControlsRow.PictureInPictureAction mPictureInPictureAction;
    protected final PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    MediaPlayer mPlayer = new MediaPlayer();
    MediaSessionCompat mMediaSession;
    PipCallback pipCallback;
    private final PlaybackControlsRow.RepeatAction mRepeatAction;
    private Runnable mRunnable;
    private Handler mHandler = new Handler();
    private boolean mInitialized = false; // true when the MediaPlayer is prepared/initialized
    private Action mSelectedAction; // the action which is currently selected by the user
    private long mLastKeyDownEvent = 0L; // timestamp when the last DPAD_CENTER KEY_DOWN occurred
    private Uri mMediaSourceUri = null;
    private String mMediaSourcePath = null;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private String mArtist;
    private String mTitle;
    private Drawable mCover;
    private PlaybackState state;
    private PlaybackStateCompat mPlaybackStateCompat;

    private boolean isInPictureInPictureMode;
    private MediaSession.Token mediaSession;

    /**
     * Sets the drawable representing cover image.
     */
    public void setCover(Drawable cover) {
        this.mCover = cover;
    }

    /**
     * Sets the artist name.
     */
    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    /**
     * Sets the media title.
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * Sets the url for the video.
     */
    public void setVideoUrl(String videoUrl) {
        setMediaSource(videoUrl);
        onMetadataChanged();
    }

    /**
     * Constructor.
     */
    public TvMediaPlayerGlue(Context context) {
        this(context, new int[]{1}, new int[]{1});
    }

    /**
     * Constructor.
     */
    public TvMediaPlayerGlue(
            Context context, int[] fastForwardSpeeds, int[] rewindSpeeds) {
        super(context, fastForwardSpeeds, rewindSpeeds);


        initMediaSession(context);

        // Instantiate secondary actions
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getContext());
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(getContext());
        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(getContext());
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(getContext());
        mPictureInPictureAction = new PlaybackControlsRow.PictureInPictureAction(getContext());

        mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
        mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);


    }

    private void initMediaSession(Context context) {
        mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f).build();
        mMediaSession = new MediaSessionCompat(context, TvMediaPlayerGlue.class.getPackage().getName());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mMediaSession.getSessionToken();
        mMediaSession.setCallback(sessionCallback);
        mMediaSession.setPlaybackState(mPlaybackStateCompat);
        mMediaSession.setActive(true);

    }

    @Override
    protected void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);
        if (host instanceof SurfaceHolderGlueHost) {
            ((SurfaceHolderGlueHost) host).setSurfaceHolderCallback(
                    new TvMediaPlayerGlue.VideoPlayerSurfaceHolderCallback());
        }
    }

    /**
     * Will reset the {@link MediaPlayer} and the glue such that a new file can be played. You are
     * not required to call this method before playing the first file. However you have to call it
     * before playing a second one.
     */
    public void reset() {
        Log.d("wjj", "===========reset============");
        changeToUnitialized();
        mPlayer.reset();
    }

    void changeToUnitialized() {
        if (mInitialized) {
            Log.d("wjj", "===========changeToUnitialized============");
            mInitialized = false;
            List<PlayerCallback> callbacks = getPlayerCallbacks();
            if (callbacks != null) {
                for (PlayerCallback callback : callbacks) {
                    callback.onPreparedStateChanged(TvMediaPlayerGlue.this);
                }
            }
        }
    }

    /**
     * Release internal MediaPlayer. Should not use the object after call release().
     */
    public void release() {
        Log.d("wjj", "===========release============");
        changeToUnitialized();
        mPlayer.release();
    }

    @Override
    protected void onDetachedFromHost() {
        if (getHost() instanceof SurfaceHolderGlueHost) {
            ((SurfaceHolderGlueHost) getHost()).setSurfaceHolderCallback(null);
        }

        reset();
        release();


        super.onDetachedFromHost();
    }

    @Override
    protected void onCreateSecondaryActions(ArrayObjectAdapter secondaryActionsAdapter) {
        secondaryActionsAdapter.add(mRepeatAction);
        secondaryActionsAdapter.add(mThumbsDownAction);
        secondaryActionsAdapter.add(mThumbsUpAction);
        secondaryActionsAdapter.add(mClosedCaptioningAction);
        secondaryActionsAdapter.add(mPictureInPictureAction);
    }

    /**
     * @see MediaPlayer#setDisplay(SurfaceHolder)
     */
    public void setDisplay(SurfaceHolder surfaceHolder) {
        mPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void enableProgressUpdating(final boolean enabled) {
        if (mRunnable != null) mHandler.removeCallbacks(mRunnable);
        if (!enabled) {
            return;
        }
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                    mHandler.postDelayed(this, getUpdatePeriod());
                }
            };
        }
        mHandler.postDelayed(mRunnable, getUpdatePeriod());
    }

    @Override
    public void onActionClicked(Action action) {
        // If either 'Shuffle' or 'Repeat' has been clicked we need to make sure the actions index
        // is incremented and the UI updated such that we can display the new state.
        super.onActionClicked(action);
        if (action instanceof PlaybackControlsRow.RepeatAction) {
            ((PlaybackControlsRow.RepeatAction) action).nextIndex();
        } else if (action == mThumbsUpAction) {
            if (mThumbsUpAction.getIndex() == PlaybackControlsRow.ThumbsAction.INDEX_SOLID) {
                mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
            } else {
                mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_SOLID);
                mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
            }
        } else if (action == mThumbsDownAction) {
            if (mThumbsDownAction.getIndex() == PlaybackControlsRow.ThumbsAction.INDEX_SOLID) {
                mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
            } else {
                mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_SOLID);
                mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
            }
        } else if (action == mPictureInPictureAction) {
            pipCallback.onEnterPictureInPictur();
        } else if (action == mClosedCaptioningAction) {

        }
        onMetadataChanged();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // This method is overridden in order to make implement fast forwarding and rewinding when
        // the user keeps the corresponding action pressed.
        // We only consume DPAD_CENTER Action_DOWN events on the Fast-Forward and Rewind action and
        // only if it has not been pressed in the last X milliseconds.
        boolean consume = mSelectedAction instanceof PlaybackControlsRow.RewindAction;
        consume = consume || mSelectedAction instanceof PlaybackControlsRow.FastForwardAction;
        consume = consume && mInitialized;
        consume = consume && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER;
        consume = consume && event.getAction() == KeyEvent.ACTION_DOWN;
        consume = consume && System
                .currentTimeMillis() - mLastKeyDownEvent > FAST_FORWARD_REWIND_REPEAT_DELAY;

        if (consume) {
            mLastKeyDownEvent = System.currentTimeMillis();
            int newPosition = getCurrentPosition() + FAST_FORWARD_REWIND_STEP;
            if (mSelectedAction instanceof PlaybackControlsRow.RewindAction) {
                newPosition = getCurrentPosition() - FAST_FORWARD_REWIND_STEP;
            }
            // Make sure the new calculated duration is in the range 0 >= X >= MediaDuration
            if (newPosition < 0) newPosition = 0;
            if (newPosition > getMediaDuration()) newPosition = getMediaDuration();
            seekTo(newPosition);
            return true;
        }

        return super.onKey(v, keyCode, event);
    }

    @Override
    public boolean hasValidMedia() {
        return mTitle != null && (mMediaSourcePath != null || mMediaSourceUri != null);
    }


    @Override
    public boolean isMediaPlaying() {
        Log.d("", "==mInitialized=" + mInitialized);
        if (mPlayer != null) {
//            Log.d("","==mInitialized="+mPlayer.isPlaying());
        }

        return mInitialized && mPlayer.isPlaying();
    }

    @Override
    public boolean isPlaying() {
        return isMediaPlaying();
    }

    @Override
    public CharSequence getMediaTitle() {
        return mTitle != null ? mTitle : "N/a";
    }

    @Override
    public CharSequence getMediaSubtitle() {
        return mArtist != null ? mArtist : "N/a";
    }

    @Override
    public int getMediaDuration() {
        return mInitialized ? mPlayer.getDuration() : 0;
    }

    @Override
    public Drawable getMediaArt() {
        return mCover;
    }

    @Override
    public long getSupportedActions() {
        return PlaybackControlGlue.ACTION_PLAY_PAUSE
                | PlaybackControlGlue.ACTION_FAST_FORWARD
                | PlaybackControlGlue.ACTION_REWIND;
    }

    @Override
    public int getCurrentSpeedId() {
        // 0 = Pause, 1 = Normal Playback Speed
        return isMediaPlaying() ? 1 : 0;
    }

    @Override
    public int getCurrentPosition() {
        return mInitialized ? mPlayer.getCurrentPosition() : 0;
    }

    @Override
    public void play(int speed) {
        if (!mInitialized || mPlayer.isPlaying()) {
            return;
        }
        Log.d("wjj", "=====play============");
        mPlayer.start();
        onMetadataChanged();
        onStateChanged();
        updateProgress();
        updatePlaybackState();
    }

    @Override
    public void pause() {
        if (isMediaPlaying()) {
            mPlayer.pause();
            onStateChanged();
        }
        updatePlaybackState();
    }

    /**
     * Sets the playback mode. It currently support no repeat, repeat once and infinite
     * loop mode.
     */
    public void setMode(int mode) {
        switch (mode) {
            case NO_REPEAT:
                mOnCompletionListener = null;
                break;
            case REPEAT_ONE:
                mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
                    public boolean mFirstRepeat;

                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (!mFirstRepeat) {
                            mFirstRepeat = true;
                            mediaPlayer.setOnCompletionListener(null);
                        }
                        play();
                    }
                };
                break;
            case REPEAT_ALL:
                mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        play();
                    }
                };
                break;
        }
    }

    /**
     * Called whenever the user presses fast-forward/rewind or when the user keeps the
     * corresponding action pressed.
     *
     * @param newPosition The new position of the media track in milliseconds.
     */
    protected void seekTo(int newPosition) {
        if (!mInitialized) {
            return;
        }
        mPlayer.seekTo(newPosition);
    }

    /**
     * Sets the media source of the player witha given URI.
     *
     * @return Returns <code>true</code> if uri represents a new media; <code>false</code>
     * otherwise.
     * @see MediaPlayer#setDataSource(String)
     */
    public boolean setMediaSource(Uri uri) {

        if (mMediaSourceUri != null ? mMediaSourceUri.equals(uri) : uri == null) {
            return false;
        }
        Log.d("wjj", "=======setMediaSource=====2======" + uri.getPath());
        mMediaSourceUri = uri;
        mMediaSourcePath = null;
        prepareMediaForPlaying();
        return true;
    }

    /**
     * Sets the media source of the player with a String path URL.
     *
     * @return Returns <code>true</code> if path represents a new media; <code>false</code>
     * otherwise.
     * @see MediaPlayer#setDataSource(String)
     */
    public boolean setMediaSource(String path) {
        if (mMediaSourcePath != null ? mMediaSourcePath.equals(path) : path == null) {
            return false;
        }
        Log.d("wjj", "=======setMediaSource====1=======" + path);
        mMediaSourceUri = null;
        mMediaSourcePath = path;
        prepareMediaForPlaying();
        return true;
    }

    private void prepareMediaForPlaying() {
        reset();

        try {
            if (mMediaSourceUri != null) {
                mPlayer.setDataSource(getContext(), mMediaSourceUri);
            } else if (mMediaSourcePath != null) {
                mPlayer.setDataSource(mMediaSourcePath);
            } else {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("wjj", "==========onPrepared============");
                mInitialized = true;
                List<PlayerCallback> callbacks = getPlayerCallbacks();
                if (callbacks != null) {
                    for (PlayerCallback callback : callbacks) {
                        callback.onPreparedStateChanged(TvMediaPlayerGlue.this);
                    }
                }
            }
        });

        if (mOnCompletionListener != null) {
            mPlayer.setOnCompletionListener(mOnCompletionListener);
        }

        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (getControlsRow() == null) {
                    return;
                }
                getControlsRow().setBufferedProgress((int) (mp.getDuration() * (percent / 100f)));
            }
        });
        mPlayer.prepareAsync();

//        try {
//            mPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        onStateChanged();
    }

    /**
     * This is a listener implementation for the {@link OnItemViewSelectedListener}.
     * This implementation is required in order to detect KEY_DOWN events
     * on the {@link android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction} and
     * {@link android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction}. Thus you
     * should <u>NOT</u> set another {@link OnItemViewSelectedListener} on your
     * Fragment. Instead, override this method and call its super (this)
     * implementation.
     *
     * @see OnItemViewSelectedListener#onItemSelected(
     *Presenter.ViewHolder, Object, RowPresenter.ViewHolder, Object)
     */
    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Action) {
            mSelectedAction = (Action) item;
        } else {
            mSelectedAction = null;
        }
    }

    @Override
    public boolean isReadyForPlayback() {
        return mInitialized;
    }

    @Override
    public boolean isPrepared() {
        return mInitialized;
    }

    public void setPipCallback() {

    }

    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    public interface PipCallback {
        void onEnterPictureInPictur();

        void onExitPictureInPictur();
    }

    public void setPipCallback(PipCallback callback) {
        pipCallback = callback;
    }

    /**
     * Implements {@link SurfaceHolder.Callback} that can then be set on the
     * {@link PlaybackGlueHost}.
     */
    class VideoPlayerSurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            setDisplay(null);
        }
    }

    MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onCommand(@NonNull String command, @Nullable Bundle args, @Nullable ResultReceiver cb) {
            super.onCommand(command, args, cb);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
            updatePlaybackState();
        }

        @Override
        public void onPlay() {
            super.onPlay();
            play();
            updatePlaybackState();

        }

        @Override
        public void onPause() {
            super.onPause();
            pause();
            updatePlaybackState();
        }

        @Override
        public void onStop() {
            super.onStop();
//            updatePlaybackState();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    };

    public void updatePlaybackState() {
        int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        Log.d("wjj", "============updatePlaybackState=====state=====" + state);
        mMediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(state, getCurrentPosition(), 1)
                        .build());
    }

    public void setInPictureInPictureMode(boolean inPictureInPictureMode) {
        isInPictureInPictureMode = inPictureInPictureMode;
    }

    @Override
    public void updateProgress() {
        int position = getCurrentPosition();
        if (getControlsRow() != null) {
            getControlsRow().setCurrentTime(position);
        }
    }
}

