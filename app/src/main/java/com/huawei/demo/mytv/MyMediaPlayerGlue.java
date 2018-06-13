package com.huawei.demo.mytv;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.widget.MediaController;

import com.huawei.demo.mytv.activity.PlaybackActivity;

import java.io.IOException;
import java.util.List;

public class MyMediaPlayerGlue extends MediaPlayerGlue {

    private PlaybackControlsRow.RepeatAction mRepeatAction;
    protected PlaybackControlsRow.PictureInPictureAction mPictureInPictureAction;
    protected PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    protected Context context;
    MediaController mediaController;

    public MyMediaPlayerGlue(Context context) {
        this(context, new int[]{1}, new int[]{1});
    }

    public MyMediaPlayerGlue(Context context, int[] fastForwardSpeeds, int[] rewindSpeeds) {
        super(context, fastForwardSpeeds, rewindSpeeds);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getContext());
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(getContext());
        mPictureInPictureAction = new PlaybackControlsRow.PictureInPictureAction(getContext());
        mClosedCaptioningAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);
    }

    @Override
    public void onActionClicked(Action action) {
        super.onActionClicked(action);
        if (action instanceof PlaybackControlsRow.RepeatAction) {
            ((PlaybackControlsRow.RepeatAction) action).nextIndex();
        } else if (action == mPictureInPictureAction) {
            ((PlaybackActivity) context).enterPictureInPictureMode();
        } else if (action == mClosedCaptioningAction) {

        }

    }

    @Override
    protected void onCreateSecondaryActions(ArrayObjectAdapter secondaryActionsAdapter) {
        secondaryActionsAdapter.add(mRepeatAction);
        secondaryActionsAdapter.add(mClosedCaptioningAction);
        secondaryActionsAdapter.add(mPictureInPictureAction);
        super.onCreateSecondaryActions(secondaryActionsAdapter);
    }


    @Override
    public boolean setMediaSource(Uri uri) {
        return super.setMediaSource(uri);
//        if (mMediaSourceUri != null ? mMediaSourceUri.equals(uri) : uri == null) {
//            return false;
//        }
//        mMediaSourceUri = uri;
//        mMediaSourcePath = null;
//        prepareMediaForPlaying();
//        return true;
    }

}
