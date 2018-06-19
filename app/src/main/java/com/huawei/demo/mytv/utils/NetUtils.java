package com.huawei.demo.mytv.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.huawei.demo.mytv.data.LocalDataManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Jack on 2018/6/16.
 */

public class NetUtils {
    private static final boolean DEBUG = LocalDataManager.getConfig().isDebug();

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
            URL url = new URL(ipAddr);
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

//    public boolean connectTest(Context context, String address){
//        Uri url = Uri.parse(address);
//        Glide.with(context).load(address);
//    }
}
