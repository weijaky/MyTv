package com.huawei.demo.mytv.handler;

import android.content.Context;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Jack on 2018/6/20.
 */

public class VideoTouchHandler implements TouchHandler, GestureDetector.OnGestureListener {
    private Context context;
    private PlaybackGlueHost glueHost;
    private GestureDetector mGestureDetector;

    public VideoTouchHandler(Context context, PlaybackGlueHost glueHost) {
        this.context = context;
        this.glueHost = glueHost;
        mGestureDetector = new GestureDetector(context, this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (glueHost.isControlsOverlayVisible()) {
            glueHost.hideControlsOverlay(true);
        } else {
            glueHost.showControlsOverlay(true);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityy) {
        Log.d("wjj", "============onFling=========");
        return true;
    }
}
