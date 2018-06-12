/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.huawei.demo.mytv.fragment;

import android.os.Bundle;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;

import com.huawei.demo.mytv.data.Movie;
import com.huawei.demo.mytv.activity.DetailsActivity;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment implements OnActionClickedListener {
    private static final String TAG = "PlaybackVideoFragment";
    private static final boolean DEBUG = true;

    private MediaPlayerGlue mMediaPlayerGlue;
    private  PlaybackControlsRow.RepeatAction mRepeatAction;
    protected  PlaybackControlsRow.PictureInPictureAction mPictureInPictureAction;
    protected  PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Movie movie = (Movie) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        VideoSupportFragmentGlueHost glueHost = new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);
        mMediaPlayerGlue = new MediaPlayerGlue(getActivity());
        mMediaPlayerGlue.setHost(glueHost);
        mMediaPlayerGlue.setMode(MediaPlayerGlue.NO_REPEAT);
        mMediaPlayerGlue.setPlayerCallback(new PlaybackGlue.PlayerCallback() {
            @Override
            public void onReadyForPlayback() {
                mMediaPlayerGlue.play();

            }
        });
        mMediaPlayerGlue.setTitle(movie.getTitle());
        mMediaPlayerGlue.setArtist(movie.getDescription());
        mMediaPlayerGlue.setVideoUrl(movie.getVideoUrl());

        initView();
        glueHost.setOnActionClickedListener(this);
    }

    private void initView() {
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getContext());
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(getContext());
        mPictureInPictureAction = new PlaybackControlsRow.PictureInPictureAction(getContext());
        mClosedCaptioningAction.setIndex(PlaybackControlsRow.ThumbsAction.INDEX_OUTLINE);

        ArrayObjectAdapter secondaryActionsAdapter = new ArrayObjectAdapter(new ControlButtonPresenterSelector());
        secondaryActionsAdapter.add(mRepeatAction);
        secondaryActionsAdapter.add(mClosedCaptioningAction);
        secondaryActionsAdapter.add(mPictureInPictureAction);
        mMediaPlayerGlue.getControlsRow().setSecondaryActionsAdapter(secondaryActionsAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue.pause();
        }
    }

    @Override
    public void onActionClicked(Action action) {
        if (action instanceof PlaybackControlsRow.RepeatAction) {
            ((PlaybackControlsRow.RepeatAction) action).nextIndex();
        } else if (action == mPictureInPictureAction) {
            getActivity().enterPictureInPictureMode();
        } else if (action == mClosedCaptioningAction) {

        }
    }
}