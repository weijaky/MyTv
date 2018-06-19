package com.huawei.demo.mytv.utils;

import android.content.Context;

import java.util.Properties;

/**
 * Created by Jack on 2018/6/16.
 */

public class ResUtils {

    public static String[] getStringArray(Context context, int resId){
         return context.getResources().getStringArray(resId);
    }

    public static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }

    public static int getRawRes(Context context, String rawName){
        return context.getResources().getIdentifier(rawName, "raw", context.getPackageName());
    }

}
