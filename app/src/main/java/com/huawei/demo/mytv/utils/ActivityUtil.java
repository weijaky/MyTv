package com.huawei.demo.mytv.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by jaky on 2017/9/15 0015.
 */

public class ActivityUtil {

    private static final String TAG = ActivityUtil.class.getSimpleName();

    public static void startActivityWithData(Context context, String pkgName, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(pkgName);
        intent.setDataAndType(Uri.fromFile(file), FileUtil.getMimeType(file.getAbsolutePath()));
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }
}
