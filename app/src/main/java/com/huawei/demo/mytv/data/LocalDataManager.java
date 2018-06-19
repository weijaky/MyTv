package com.huawei.demo.mytv.data;

import android.content.Context;

import com.huawei.demo.mytv.TvApplication;
import com.huawei.demo.mytv.utils.ResUtils;

import java.util.Properties;

/**
 * Created by Jack on 2018/6/16.
 */

public class LocalDataManager {
    private static Properties props;
    private static LocalConfig config;

    public static LocalConfig getConfig() {
        if (config == null) {
            config = loadLocalConfig(TvApplication.getsInstance());
        }
        return config;
    }

    public static Properties getProps() {
        if (props == null) {
            props = loadProperties(TvApplication.getsInstance());
        }
        return props;
    }

    public static void init(Context context) {
        props = loadProperties(context);
        config = loadLocalConfig(context);
    }

    private static LocalConfig loadLocalConfig(Context context) {
        if (config == null) {
            config = LocalConfig.getConfig();
        }
        return config;
    }


    private static Properties loadProperties(Context context) {
        Properties props = new Properties();
        try {
            int id = ResUtils.getRawRes(context, "properties");
            props.load(context.getResources().openRawResource(id));
        } catch (Exception e) {
        }
        return props;
    }
}
