/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.huawei.demo.mytv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.huawei.demo.mytv.fragment.PlaybackVideoFragment;
import com.huawei.demo.mytv.handler.TouchHandler;

import java.util.ArrayList;

/**
 * Loads {@link PlaybackVideoFragment}.
 */
public class PlaybackActivity extends FragmentActivity {

    private PlaybackVideoFragment mPlaybackVideoFragment;
    private ArrayList<TouchHandler> mTouchHandlers = new ArrayList<TouchHandler>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mPlaybackVideoFragment = new PlaybackVideoFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, mPlaybackVideoFragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPlaybackVideoFragment.setVideo(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (TouchHandler handler : mTouchHandlers) {
            if (handler.onTouchEvent(ev)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void registerTouchHandler(TouchHandler listener) {
        mTouchHandlers.add(listener);
    }

    public void unRegisterTouchHandler(TouchHandler listener) {
        mTouchHandlers.remove(listener);
    }
}