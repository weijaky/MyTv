package com.huawei.demo.mytv.handler;

import android.content.Context;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Jack on 2018/6/20.
 */

public class SurfaceViewTouchHandler implements SurfaceView.OnTouchListener {
    private Context context;
    private PlaybackGlueHost glueHost;

    public SurfaceViewTouchHandler(Context context, PlaybackGlueHost glueHost) {
        this.context = context;
        this.glueHost = glueHost;

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("wjj", "======onTouch=====ACTION_DOWN========");

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d("wjj", "======onTouch=====ACTION_DOWN========" + event.getX());
            glueHost.showControlsOverlay(true);
            return true;
        }
        return v.onTouchEvent(event);
    }
}
