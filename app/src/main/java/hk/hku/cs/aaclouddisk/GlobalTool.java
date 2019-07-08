package hk.hku.cs.aaclouddisk;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static android.os.Environment.MEDIA_MOUNTED;

public class GlobalTool {

    private static final String INDIVIDUAL_DIR_NAME = "video-cache";

    /**
     * Transform time in seconds to "mm:ss" String
     * @param second
     * @return
     */
    public static String secondToMinSecText(int second) {
        String min = "" + second / 60;
        String sec = "" + second % 60;
        if (min.length() == 1)
            min = "0" + min;
        if (sec.length() == 1)
            sec = "0" + sec;
        return min + ":" + sec;
    }

    /**
     * Delete a Dir and all file and dirs in it
     * @param dir the target Dir
     */
    public static void deleteDir(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            for (File file: dir.listFiles()) {
                deleteDir(file);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    public static Scanner getFileScanner(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return scanner;
    }

    public static PrintStream getFilePrintStream(String path) {
        File file = new File(path);
        if (file.exists()) {
            GlobalTool.deleteDir(file);
        }

        boolean initIOSucc;
        PrintStream output = null;
        try {
            initIOSucc = file.createNewFile();
            output = new PrintStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            initIOSucc =false;
        }

        if (!initIOSucc) {
            return null;
        }

        return output;
    }

    /**
     * Return a child dir in cache folder, prefer not SD Card
     *
     * @param context Application context
     * @param childDirName A Child dir (dirName)
     * @return Child cache file directory
     */
    public static File getChildCacheDirectory(Context context, String childDirName) {
        File cacheDir = getCacheDirectory(context, false);
        return new File(cacheDir, childDirName);
    }

    /**
     * Returns individual application cache directory (for only video caching from Proxy). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/video-cache")</i> if card is mounted .
     * Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context) {
        File cacheDir = getCacheDirectory(context, true);
        return new File(cacheDir, INDIVIDUAL_DIR_NAME);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    private static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Log.w("shijian", "Can't define system cache directory! '" + cacheDirPath + "%s' will be used.");
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w("shijian", "Unable to create external cache directory");
                return null;
            }
        }
        return appCacheDir;
    }

}
