package com.huawei.demo.mytv.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.huawei.demo.mytv.data.LocalDataManager;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jack on 2018/6/16.
 */

public class NetUtils {
    private static final boolean DEBUG = LocalDataManager.getConfig().isDebug();
    public static final String MEDIA_PATTERN = "(http[s]?://)+([\\w-]+\\.)+[\\w-]+([\\w-./?%&=]*)?";

    public static boolean pingTest(String address) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ping " + address);
            InputStreamReader r = new InputStreamReader(process.getInputStream());
            LineNumberReader returnData = new LineNumberReader(r);
            String returnMsg = "";
            String line = "";
            while ((line = returnData.readLine()) != null) {
                if (DEBUG) Log.d("wjj", line);
                returnMsg += line;
            }

            if (returnMsg.indexOf("100% loss") != -1) {
                if (DEBUG) Log.d("wjj", "与 " + address + " 连接不畅通.");
                return false;
            } else {
                if (DEBUG) Log.d("wjj", "与 " + address + " 连接畅通.");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean isConnected(String ipAddr) {

        boolean isConn = false;
        HttpURLConnection conn = null;

        try {
            URL url = new URL(ipAddr +
                    LocalDataManager.getConfig().getMovieDir() +
                    LocalDataManager.getConfig().getImageDir() +
                    File.separator +
                    LocalDataManager.getConfig().getTestRes());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            if (conn.getResponseCode() == 200) {
                isConn = true;
            }
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } finally {
            conn.disconnect();
        }
        return isConn;
    }

    public static Uri getIntentUri(Intent intent) {
        Uri result = null;
        if (intent != null) {
            result = intent.getData();
            if (result == null) {
                final String type = intent.getType();
                String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (!StringUtils.isEmpty(sharedUrl)) {
                    if ("text/plain".equals(type) && sharedUrl != null) {
                        result = getTextUri(sharedUrl);
                    } else if ("text/html".equals(type) && sharedUrl != null) {
                        result = getTextUri(Html.fromHtml(sharedUrl).toString());
                    }
                } else {
                    Parcelable parce = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (parce != null)
                        result = (Uri) parce;
                }
            }
        }
        return result;
    }

    private static Uri getTextUri(String sharedUrl) {
        Pattern pattern = Pattern.compile(MEDIA_PATTERN);
        Matcher matcher = pattern.matcher(sharedUrl);
        if (matcher.find()) {
            sharedUrl = matcher.group();
            if (!StringUtils.isEmpty(sharedUrl)) {
                return Uri.parse(sharedUrl);
            }
        }
        return null;
    }
}
