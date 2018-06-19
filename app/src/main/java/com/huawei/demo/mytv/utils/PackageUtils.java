package com.huawei.demo.mytv.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by jaky on 2017/12/4 0004.
 */

public class PackageUtils {

    public static final String TAG = "PackageUtils";
    public static final int APP_INSTALL_AUTO = 0;
    public static final int APP_INSTALL_INTERNAL = 1;
    public static final int APP_INSTALL_EXTERNAL = 2;

    public static final String APP_CHANNEL = "channel";
    public static final String APP_TYPE = "type";
    public static final String APP_TYPE_RELEASE = "RELEASE";
    public static final String APP_TYPE_DEBUG = "DEBUG";
    public static final String APP_PLATFORM = "platform";
    public static final String APP_TIMESTAMP = "timestamp";

    public static Intent installIntent(File file) {
        ComponentName componentName;
        if (Build.VERSION.SDK_INT < 23) {
            componentName = new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
        } else {
            componentName = new ComponentName("com.google.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setComponent(componentName);
        i.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return i;
    }

    public static final int install(Context context, String filePath) {
        if (PackageUtils.isSystemApplication(context) || ShellUtils.checkRootPermission()) {
            return installSilent(context, filePath);
        }
        return installNormal(context, filePath) ? INSTALL_SUCCEEDED : INSTALL_FAILED_INVALID_URI;
    }

    public static boolean installNormal(Context context, String filePath) {
        File file = new File(filePath);
        if (file == null || !file.exists() || !file.isFile() || file.length() <= 0) {
            return false;
        }
        Intent i = installIntent(file);
        context.startActivity(i);
        return true;
    }

    public static int installSilent(Context context, String filePath) {
        return installSilent(context, filePath, " -r " + getInstallLocationParams());
    }

    public static int installSilent(Context context, String filePath, String pmParams) {
        if (filePath == null || filePath.length() == 0) {
            return INSTALL_FAILED_INVALID_URI;
        }

        File file = new File(filePath);
        if (file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return INSTALL_FAILED_INVALID_URI;
        }

        StringBuilder command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install ")
                .append(pmParams == null ? "" : pmParams).append(" ").append(filePath.replace(" ", "\\ "));
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command.toString(), !isSystemApplication(context), true);
        if (commandResult.successMsg != null
                && (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
            return INSTALL_SUCCEEDED;
        }

        Log.e(TAG,
                new StringBuilder().append("installSilent successMsg:").append(commandResult.successMsg)
                        .append(", ErrorMsg:").append(commandResult.errorMsg).toString());
        if (commandResult.errorMsg == null) {
            return INSTALL_FAILED_OTHER;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_ALREADY_EXISTS")) {
            return INSTALL_FAILED_ALREADY_EXISTS;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_APK")) {
            return INSTALL_FAILED_INVALID_APK;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_URI")) {
            return INSTALL_FAILED_INVALID_URI;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
            return INSTALL_FAILED_INSUFFICIENT_STORAGE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_DUPLICATE_PACKAGE")) {
            return INSTALL_FAILED_DUPLICATE_PACKAGE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_NO_SHARED_USER")) {
            return INSTALL_FAILED_NO_SHARED_USER;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
            return INSTALL_FAILED_UPDATE_INCOMPATIBLE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE")) {
            return INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
            return INSTALL_FAILED_MISSING_SHARED_LIBRARY;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_REPLACE_COULDNT_DELETE")) {
            return INSTALL_FAILED_REPLACE_COULDNT_DELETE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_DEXOPT")) {
            return INSTALL_FAILED_DEXOPT;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_OLDER_SDK")) {
            return INSTALL_FAILED_OLDER_SDK;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_CONFLICTING_PROVIDER")) {
            return INSTALL_FAILED_CONFLICTING_PROVIDER;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_NEWER_SDK")) {
            return INSTALL_FAILED_NEWER_SDK;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_TEST_ONLY")) {
            return INSTALL_FAILED_TEST_ONLY;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE")) {
            return INSTALL_FAILED_CPU_ABI_INCOMPATIBLE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_FEATURE")) {
            return INSTALL_FAILED_MISSING_FEATURE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_CONTAINER_ERROR")) {
            return INSTALL_FAILED_CONTAINER_ERROR;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_INSTALL_LOCATION")) {
            return INSTALL_FAILED_INVALID_INSTALL_LOCATION;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_MEDIA_UNAVAILABLE")) {
            return INSTALL_FAILED_MEDIA_UNAVAILABLE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_TIMEOUT")) {
            return INSTALL_FAILED_VERIFICATION_TIMEOUT;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_FAILURE")) {
            return INSTALL_FAILED_VERIFICATION_FAILURE;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_PACKAGE_CHANGED")) {
            return INSTALL_FAILED_PACKAGE_CHANGED;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_UID_CHANGED")) {
            return INSTALL_FAILED_UID_CHANGED;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NOT_APK")) {
            return INSTALL_PARSE_FAILED_NOT_APK;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_MANIFEST")) {
            return INSTALL_PARSE_FAILED_BAD_MANIFEST;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION")) {
            return INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NO_CERTIFICATES")) {
            return INSTALL_PARSE_FAILED_NO_CERTIFICATES;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES")) {
            return INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING")) {
            return INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME")) {
            return INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID")) {
            return INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_MALFORMED")) {
            return INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        }
        if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_EMPTY")) {
            return INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
        }
        if (commandResult.errorMsg.contains("INSTALL_FAILED_INTERNAL_ERROR")) {
            return INSTALL_FAILED_INTERNAL_ERROR;
        }
        return INSTALL_FAILED_OTHER;
    }

    public static final int uninstall(Context context, String packageName) {
        if (PackageUtils.isSystemApplication(context) || ShellUtils.checkRootPermission()) {
            return uninstallSilent(context, packageName);
        }
        return uninstallNormal(context, packageName) ? DELETE_SUCCEEDED : DELETE_FAILED_INVALID_PACKAGE;
    }

    public static boolean uninstallNormal(Context context, String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return false;
        }

        Intent i = new Intent(Intent.ACTION_DELETE, Uri.parse(new StringBuilder(32).append("package:")
                .append(packageName).toString()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        return true;
    }

    public static int uninstallSilent(Context context, String packageName) {
        return uninstallSilent(context, packageName, true);
    }

    public static int uninstallSilent(Context context, String packageName, boolean isKeepData) {
        if (packageName == null || packageName.length() == 0) {
            return DELETE_FAILED_INVALID_PACKAGE;
        }

        StringBuilder command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall")
                .append(isKeepData ? " -k " : " ").append(packageName.replace(" ", "\\ "));
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command.toString(), !isSystemApplication(context), true);
        if (commandResult.successMsg != null
                && (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
            return DELETE_SUCCEEDED;
        }
        Log.e(TAG,
                new StringBuilder().append("uninstallSilent successMsg:").append(commandResult.successMsg)
                        .append(", ErrorMsg:").append(commandResult.errorMsg).toString());
        if (commandResult.errorMsg == null) {
            return DELETE_FAILED_INTERNAL_ERROR;
        }
        if (commandResult.errorMsg.contains("Permission denied")) {
            return DELETE_FAILED_PERMISSION_DENIED;
        }
        return DELETE_FAILED_INTERNAL_ERROR;
    }

    public static boolean isSystemApplication(Context context) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context, context.getPackageName());
    }

    public static boolean isSystemApplication(Context context, String packageName) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context.getPackageManager(), packageName);
    }

    public static boolean isSystemApplication(PackageManager packageManager, String packageName) {
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }

        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean isTopActivity(Context context, String packageName) {
        if (context == null || StringUtils.isBlank(packageName)) {
            return null;
        }

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo == null || tasksInfo.size() <= 0) {
            return null;
        }
        try {
            return packageName.equals(tasksInfo.get(0).topActivity.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getAppVersionCode(Context context) {
        return getAppVersionCode(context, context.getPackageName());
    }

    public static int getInstallLocation() {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm get-install-location", false, true);
        if (commandResult.result == 0 && commandResult.successMsg != null && commandResult.successMsg.length() > 0) {
            try {
                int location = Integer.parseInt(commandResult.successMsg.substring(0, 1));
                switch (location) {
                    case APP_INSTALL_INTERNAL:
                        return APP_INSTALL_INTERNAL;
                    case APP_INSTALL_EXTERNAL:
                        return APP_INSTALL_EXTERNAL;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.e(TAG, "pm get-install-location error");
            }
        }
        return APP_INSTALL_AUTO;
    }

    private static String getInstallLocationParams() {
        int location = getInstallLocation();
        switch (location) {
            case APP_INSTALL_INTERNAL:
                return "-f";
            case APP_INSTALL_EXTERNAL:
                return "-s";
        }
        return "";
    }

    public static void startInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        int sdkVersion = Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra((sdkVersion == Build.VERSION_CODES.FROYO ? "pkg"
                    : "com.android.settings.ApplicationPkgName"), packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static final int INSTALL_SUCCEEDED = 1;

    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

    public static final int INSTALL_FAILED_INVALID_APK = -2;

    public static final int INSTALL_FAILED_INVALID_URI = -3;

    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

    public static final int INSTALL_FAILED_DEXOPT = -11;

    public static final int INSTALL_FAILED_OLDER_SDK = -12;

    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

    public static final int INSTALL_FAILED_NEWER_SDK = -14;

    public static final int INSTALL_FAILED_TEST_ONLY = -15;

    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;

    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;

    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;

    public static final int INSTALL_FAILED_UID_CHANGED = -24;

    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;

    public static final int INSTALL_FAILED_OTHER = -1000000;

    public static final int DELETE_SUCCEEDED = 1;

    public static final int DELETE_FAILED_INTERNAL_ERROR = -1;

    public static final int DELETE_FAILED_DEVICE_POLICY_MANAGER = -2;

    public static final int DELETE_FAILED_INVALID_PACKAGE = -3;

    public static final int DELETE_FAILED_PERMISSION_DENIED = -4;


    public static boolean checkAppInstalled(Context context, String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getAppVersionName(Context context) {
        return getAppVersionName(context, context.getPackageName());
    }

    public static String getAppVersionName(Context context, String packageName) {
        String versionName = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getAppVersionCode(Context context, String packageName) {
        int versionCode = 1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getAppDisplayName(Context context, String packageName) {
        String applicationLabel = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            applicationLabel = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationLabel;
    }

    public static Drawable getAppIconDrawable(Context context, String packageName) {
        Drawable drawable = null;
        try {
            drawable = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public static String getAppType(Context context) {
        return getAppType(context, context.getPackageName());
    }

    public static String getAppType(Context context, String packageName) {
        String type = getApplicationMetaData(context, packageName, APP_TYPE);
        if (StringUtils.isNotBlank(type)) {
            return type;
        }
        return APP_TYPE_RELEASE;
    }

    public static String getAppChannel(Context context) {
        return getAppChannel(context, context.getPackageName());
    }

    public static String getAppChannel(Context context, String packageName) {
        String channel = getApplicationMetaData(context, packageName, APP_CHANNEL);
        if (StringUtils.isNotBlank(channel)) {
            return channel;
        }
        return null;
    }

    public static String getAppPlatform(Context context) {
        return getAppPlatform(context, context.getPackageName());
    }

    public static String getAppPlatform(Context context, String packageName) {
        String platform = getApplicationMetaData(context, packageName, APP_PLATFORM);
        if (StringUtils.isNotBlank(platform)) {
            return platform;
        }
        return null;
    }

    private static long parseLong(String longString) {
        long digital = -1;
        try {
            digital = Long.parseLong(longString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return digital;
    }

    public static Date getAppTimestamp(Context context, String packageName) {
        String timestamp = getApplicationMetaData(context, packageName, APP_TIMESTAMP);
        if (StringUtils.isBlank(timestamp)) {
            return getApkFileCreatedTime(context, packageName);
        }
        long time = parseLong(timestamp.replaceAll("L", ""));
        return time <= 0 ? null : new Date(time);
    }

    public static String getApplicationMetaData(Context context, String packageName, String key) {
        String metadata = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData == null) {
                return null;
            }
            String value = appInfo.metaData.getString(key);
            if (StringUtils.isNotBlank(value)) {
                metadata = value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    private static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static Date getApkFileCreatedTime(Context context, String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(context, packageName);
        if (applicationInfo == null) {
            return null;
        }
        Date createdTime = null;
        File file = new File(applicationInfo.publicSourceDir);
        if (file.exists()) {
            long time = file.lastModified();
            if (time > 0) {
                createdTime = new Date(time);
            }
        }
        return createdTime;
    }

    public static int getApkFileSize(Context context, String packageName) {
        int size = 0;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            File file = new File(applicationInfo.publicSourceDir);
            if (file.exists()) {
                size = (int) file.length();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return size;
    }

    //暂时不可用
    public static PackageInfo getApkInfo(Context context, String path) {
        if (StringUtils.isBlank(path) || !new File(path).exists() || !path.endsWith(".apk")) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
    }

}
