package com.caozx.extraction.manager;

import android.os.Environment;

import com.caozx.extraction.APP;

import java.io.File;

/**
 * 目录管理器
 */
public class FolderManager {

    /**
     * 应用程序在SD卡上的主目录名称
     */
    private final static String FOLDER_NAME_ROOT = APP.getInstance().getPackageName();
    /**
     * 存放图片目录名
     */
    private final static String FOLDER_NAME_PIC = "picture";
    /**
     * 存放闪退日志目录名
     */
    private final static String FOLDER_NAME_CRASH = "crash";


    private FolderManager() {
    }

    /**
     * 获取app在sd卡上的主目录
     *
     * @return 成功则返回目录，失败则返回null
     */
    public static File getAppFolder() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File appFolder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME_ROOT);
            return createOnNotFound(appFolder);

        } else {
            return null;
        }
    }

    /**
     * 获取应用存放图片的目录
     *
     * @return 成功则返回目录名，失败则返回null
     */
    public static File getPicFolder() {
        File appFolder = getAppFolder();
        if (appFolder != null) {

            File photoFolder = new File(appFolder, FOLDER_NAME_PIC);
            return createOnNotFound(photoFolder);

        } else {
            return null;
        }
    }

    /**
     * 获取闪退日志存放目录
     *
     * @return
     */
    public static File getCrashLogFolder() {
        File appFolder = getAppFolder();
        if (appFolder != null) {

            File crashLogFolder = new File(appFolder, FOLDER_NAME_CRASH);
            return createOnNotFound(crashLogFolder);
        } else {
            return null;
        }
    }

    /**
     * 创建目录
     *
     * @param folder
     * @return 创建成功则返回目录，失败则返回null
     */
    private static File createOnNotFound(File folder) {
        if (folder == null) {
            return null;
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (folder.exists()) {
            return folder;
        } else {
            return null;
        }
    }
}
