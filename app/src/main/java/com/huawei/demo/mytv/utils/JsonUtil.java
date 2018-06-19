package com.huawei.demo.mytv.utils;

import android.text.TextUtils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;


/**
 * Created by jaky on 2017/9/8 0008.
 */

public class JsonUtil {

    public static String objectToJson(Object object) {
        if (object == null) {
            return "";
        }
        try {
            return JSON.toJSONString(object);
        } catch (JSONException e) {
        } catch (Exception e) {
        }
        return "";
    }

    public static <T> T jsonToObject(String jsonData, Class<T> clazz) {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }
        try {
            return parseObject(jsonData, clazz);
        } catch (Exception e) {
        }
        return null;
    }

    public static List jsonToList(String jsonData) {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }
        List arrayList = null;
        try {
            arrayList = parseObject(jsonData, new TypeReference<ArrayList>() {
            });
        } catch (Exception e) {
        }
        return arrayList;

    }

    public static Map jsonToMap(String jsonData) {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }
        Map map = null;
        try {
            map = parseObject(jsonData, new TypeReference<Map>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
