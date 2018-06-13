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
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.widget.MediaController;

import com.huawei.demo.mytv.MyMediaPlayerGlue;
import com.huawei.demo.mytv.data.Config;
import com.huawei.demo.mytv.data.Movie;
import com.huawei.demo.mytv.activity.DetailsActivity;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment
        implements OnItemViewSelectedListener
{
    private static final String TAG = "PlaybackVideoFragment";
    private static final boolean DEBUG = Config.DEBUG;

    private MyMediaPlayerGlue mMediaPlayerGlue;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Movie movie = (Movie) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        VideoSupportFragmentGlueHost glueHost = new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);
        mMediaPlayerGlue = new MyMediaPlayerGlue(getActivity());
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
        String url = movie.getVideoUrl();
        Log.d("wjj", "==========url=====" + url);
        mMediaPlayerGlue.setVideoUrl(url);
        MediaController mc = new MediaController(getActivity());

//        mc.setMediaPlayer(mMediaPlayerGlue);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().isInPictureInPictureMode()) {
            if (mMediaPlayerGlue != null) {
                mMediaPlayerGlue.play();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().isInPictureInPictureMode()) {

            if (mMediaPlayerGlue != null) {
                mMediaPlayerGlue.pause();
            }
        }

    }



    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }
}