
package com.huawei.demo.mytv.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

import com.huawei.demo.mytv.data.LocalConfig;

import java.io.IOException;
import java.util.List;

public class TvMediaPlayerManager extends PlaybackControlGlue implements OnItemViewSelectedListener {

    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;

    private static final String TAG = TvMediaPlayerManager.class.getSimpleName();
    private static final boolean DEBUG = LocalConfig.getConfig().isDebug();
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

    public static final int FAST_FORWARD_REWIND_STEP = 10 * 1000;
    public static final int FAST_FORWARD_REWIND_REPEAT_DELAY = 200;


    protected PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    protected PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    protected PlaybackControlsRow.PictureInPictureAction mPictureInPictureAction;
    protected PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    protected PlaybackControlsRow.RepeatAction mRepeatAction;
    protected MediaPlayer mPlayer = new MediaPlayer();
    protected MediaSessionCompat mMediaSession;
    protected PipCallback pipCallback;

    private Runnable mRunnable;
    private Handler mHandler = new Handler();
    private boolean mInitialized = false;
    private Action mSelectedAction;
    private long mLastKeyDownEvent = 0L;
    private Uri mMediaSourceUri = null;
    private String mMediaSourcePath = null;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private String mArtist;
    private String mTitle;
    private Drawable mCover;
    private PlaybackState state;
    private PlaybackStateCompat mPlaybackStateCompat;

    private boolean isInPictureInPictureMode;


    public void setCover(Drawable cover) {
        this.mCover = cover;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setVideoUrl(String videoUrl) {
        setMediaSource(videoUrl);
        onMetadataChanged();
    }

    public void setVideoUri(Uri videoUri) {
        setMediaSource(videoUri);
        onMetadataChanged();
    }

    public TvMediaPlayerManager(Context context) {
        this(context, new int[]{1}, new int[]{1});
    }

    public TvMediaPlayerManager(Context context, int[] fastForwardSpeeds, int[] rewindSpeeds) {
        super(context, fastForwardSpeeds, rewindSpeeds);
        initMediaSession(context);
        initActions();
    }

    private void initActions() {
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getContext());
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(getContext());
        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(getContext());
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(getContext());
        mPictureInPictureAction = new PlaybackControlsRow.PictureInPictureAction(getContext());

        mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
        mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
    }

    private void initMediaSession(Context context) {
        if (mMediaSession != null) {
            return;
        }
        if (DEBUG) Log.d(TAG, "=========initMediaSession==================");
        mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f).build();

        mMediaSession = new MediaSessionCompat(context, TvMediaPlayerManager.class.getPackage().getName());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
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
                    new TvMediaPlayerManager.VideoPlayerSurfaceHolderCallback());
        }
    }

    public void reset() {
        if (DEBUG) Log.d(TAG, "===========reset============");
        changeToUnitialized();
        mPlayer.reset();
    }

    void changeToUnitialized() {
        if (mInitialized) {
            if (DEBUG) Log.d(TAG, "===========changeToUnitialized============");
            mInitialized = false;
            List<PlayerCallback> callbacks = getPlayerCallbacks();
            if (callbacks != null) {
                for (PlayerCallback callback : callbacks) {
                    callback.onPreparedStateChanged(TvMediaPlayerManager.this);
                }
            }
        }
    }

    public void release() {
        if (DEBUG) Log.d(TAG, "===========release============");
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
        secondaryActionsAdapter.add(mPictureInPictureAction);
        secondaryActionsAdapter.add(mClosedCaptioningAction);

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
        //如果按下快退或者快进键
        boolean consume = mSelectedAction instanceof PlaybackControlsRow.RewindAction ||
                mSelectedAction instanceof PlaybackControlsRow.FastForwardAction;
        consume = consume && mInitialized &&
                event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                System.currentTimeMillis() - mLastKeyDownEvent > FAST_FORWARD_REWIND_REPEAT_DELAY;

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

        //其他情况
        return super.onKey(v, keyCode, event);
    }

    @Override
    public boolean hasValidMedia() {
        return mTitle != null && (mMediaSourcePath != null || mMediaSourceUri != null);
    }


    @Override
    public boolean isMediaPlaying() {
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
        if(DEBUG) Log.d(TAG, "=====play============");
        mPlayer.start();
        onMetadataChanged();
        onStateChanged();
        updateProgress();
        updatePlaybackState();
    }

    @Override
    public void pause() {
        if (isMediaPlaying()) {
            if(DEBUG) Log.d(TAG, "=====pause============");
            mPlayer.pause();
            onStateChanged();
        }
        updatePlaybackState();
    }

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

    protected void seekTo(int newPosition) {
        if (!mInitialized) {
            return;
        }
        mPlayer.seekTo(newPosition);
    }

    public boolean setMediaSource(Uri uri) {

        if (mMediaSourceUri != null ? mMediaSourceUri.equals(uri) : uri == null) {
            return false;
        }
        mMediaSourceUri = uri;
        mMediaSourcePath = null;
        prepareMediaForPlaying();
        return true;
    }

    public boolean setMediaSource(String path) {
        if (mMediaSourcePath != null ? mMediaSourcePath.equals(path) : path == null) {
            return false;
        }
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
                mInitialized = true;
                List<PlayerCallback> callbacks = getPlayerCallbacks();
                if (callbacks != null) {
                    for (PlayerCallback callback : callbacks) {
                        callback.onPreparedStateChanged(TvMediaPlayerManager.this);
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

        onStateChanged();
    }

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
            updatePlaybackState();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    };

    public void updatePlaybackState() {
        int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        if(DEBUG) Log.d(TAG, "============updatePlaybackState=====state=====" + state);
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

    public int getVideoWidth() {
        return mPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mPlayer.getVideoHeight();
    }


}

