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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.huawei.demo.mytv.data.LocalDataManager;
import com.huawei.demo.mytv.handler.SurfaceViewTouchHandler;
import com.huawei.demo.mytv.manager.TvMediaPlayerManager;
import com.huawei.demo.mytv.activity.DetailsActivity;
import com.huawei.demo.mytv.data.Movie;
import com.huawei.demo.mytv.utils.NetUtils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends VideoSupportFragment {
    private static final String TAG = "PlaybackVideoFragment";
    private static final boolean DEBUG = LocalDataManager.getConfig().isDebug();

    private TvMediaPlayerManager mMediaPlayerGlue;
    private MediaControllerCompat mMediaController;

    private Uri uri;
    private VideoSupportFragmentGlueHost glueHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glueHost = new VideoSupportFragmentGlueHost(PlaybackVideoFragment.this);
        initMediaGlue(glueHost);
    }

    private void initMediaGlue(VideoSupportFragmentGlueHost glueHost) {
        if (mMediaPlayerGlue == null) {
            mMediaPlayerGlue = new TvMediaPlayerManager(getActivity());
        }

        mMediaPlayerGlue.setHost(glueHost);
        mMediaPlayerGlue.setMode(MediaPlayerGlue.NO_REPEAT);
        mMediaPlayerGlue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {

            @Override
            public void onPreparedStateChanged(PlaybackGlue glue) {
                super.onPreparedStateChanged(glue);
                TvMediaPlayerManager manager = (TvMediaPlayerManager) glue;
                int vWidth = manager.getVideoWidth();
                int vHeight = manager.getVideoHeight();

                Log.d("wjj", "============vWidth====" + vWidth + "==vHeight==" + vHeight);
                mMediaPlayerGlue.play();
            }
        });

        mMediaPlayerGlue.setPipCallback(new TvMediaPlayerManager.PipCallback() {
            @Override
            public void onEnterPictureInPictur() {
                getActivity().enterPictureInPictureMode();
            }

            @Override
            public void onExitPictureInPictur() {

            }
        });

        initMediaController();
        setVideo(getActivity().getIntent());

    }

    public void setVideo(Intent intent) {
        if (intent == null) {
            return;
        }
        uri = NetUtils.getIntentUri(intent);


        if (uri != null) {
            if(DEBUG) Log.d(TAG, "==============" + uri.getPath());
            checkSelfPermission();
        } else {
            final Movie movie = (Movie) intent.getSerializableExtra(DetailsActivity.MOVIE);
            mMediaPlayerGlue.setTitle(movie.getTitle());
            mMediaPlayerGlue.setArtist(movie.getDescription());
            mMediaPlayerGlue.setVideoUrl(movie.getVideoUrl());
        }

    }

    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        } else {
            mMediaPlayerGlue.setVideoUri(uri);
        }
    }

    private void initMediaController() {
        try {
            mMediaController = new MediaControllerCompat(getActivity(), mMediaPlayerGlue.getMediaSession().getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mMediaController != null) {
            mMediaController.registerCallback(new MediaControllerCompat.Callback() {
                @Override
                public void onSessionDestroyed() {
                    super.onSessionDestroyed();
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                }
            });
        }
    }

    public MediaControllerCompat getMediaController() {
        return mMediaController;
    }

    @Override
    public void onStart() {
        super.onStart();
//        getSurfaceView().setOnTouchListener(new SurfaceViewTouchHandler(getActivity(), glueHost));
    }

    @Override
    public void onStop() {
        super.onStop();
        if(getActivity().isInPictureInPictureMode()){
            mMediaPlayerGlue.pause();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        mMediaPlayerGlue.setInPictureInPictureMode(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {

        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMediaPlayerGlue.setVideoUrl(uri.getPath());
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}