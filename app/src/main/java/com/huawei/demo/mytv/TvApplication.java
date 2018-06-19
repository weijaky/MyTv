package com.huawei.demo.mytv;

import android.app.Application;

import com.huawei.demo.mytv.data.LocalConfig;
import com.huawei.demo.mytv.data.LocalDataManager;

/**
 * Created by Jack on 2018/6/16.
 */

public class TvApplication extends Application{

    public static TvApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        LocalDataManager.init(getApplicationContext());
    }

    public static TvApplication getsInstance() {
        return sInstance;
    }
}
