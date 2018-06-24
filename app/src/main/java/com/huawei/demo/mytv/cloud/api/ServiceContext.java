package com.huawei.demo.mytv.cloud.api;

import com.huawei.demo.mytv.data.LocalConfig;

/**
 * Created by Jack on 2018/6/22.
 */

public class ServiceContext {

    public static final String SERVER_TOKEN = "X-Auth-Token";
    public static final String CATEGORY = "category";
    public static final String BASEURL = LocalConfig.getConfig().getServer();
}
