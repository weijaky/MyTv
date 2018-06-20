package com.huawei.demo.mytv.data;

import android.util.Log;

import com.huawei.demo.mytv.TvApplication;
import com.huawei.demo.mytv.utils.FileUtils;
import com.huawei.demo.mytv.utils.JsonUtil;
import com.huawei.demo.mytv.utils.NetUtils;
import com.huawei.demo.mytv.utils.ResUtils;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Jack on 2018/6/16.
 */

public class LocalConfig {

    private static LocalConfig config;
    private List<String> serverUrl;
    private static String server;
    private List<String> movieCategory;
    private String movieDir;
    private String imageDir;
    private List<String> movieTitle;
    private List<String> movieDescription;
    private List<String> studio;
    private boolean debug = false;

    public static LocalConfig getConfig() {
        if (config == null) {
            int id = ResUtils.getRawRes(TvApplication.getsInstance(), "local_config");
            config = load(TvApplication.getsInstance().getResources().openRawResource(id));
        }
        return config;
    }

    private static LocalConfig load(InputStream inputStream) {
        String content = FileUtils.readContentByStream(inputStream);
        return JsonUtil.jsonToObject(content, LocalConfig.class);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getServer() {

        List<String> servers = getServerUrl();

        if ((server == null || server.isEmpty()) && servers != null && servers.size() > 0) {
            for (int i = 0; i < servers.size(); i++) {
                if (servers.get(i) == null) {
                    continue;
                }
                Log.d("wjj", "=====servers==========" + servers.get(i));
                if (NetUtils.isConnected(servers.get(i))) {
                    server = servers.get(i);
                    Log.d("wjj", "=====Connected==========" + server);
                    return server;
                }
            }
        }
        server = "http://www.weijaky.top";
        return server;
    }

    public String getMovieDir() {
        return movieDir;
    }

    public void setMovieDir(String movieDir) {
        this.movieDir = movieDir;
    }

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public List<String> getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(List<String> serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<String> getMovieCategory() {
        return movieCategory;
    }

    public void setMovieCategory(List<String> movieCategory) {
        this.movieCategory = movieCategory;
    }

    public List<String> getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(List<String> movieTitle) {
        this.movieTitle = movieTitle;
    }

    public List<String> getMovieDescription() {
        return movieDescription;
    }

    public void setMovieDescription(List<String> movieDescription) {
        this.movieDescription = movieDescription;
    }

    public List<String> getStudio() {
        return studio;
    }

    public void setStudio(List<String> studio) {
        this.studio = studio;
    }
}
