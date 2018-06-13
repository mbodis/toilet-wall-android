package com.svb.toiletwall.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLog {

    public static final boolean enable = true;

    public static final String FILE_DEFAULT = "default_log.txt";
    public static final String TAG = "FileLog";

    public static final String[] ALL_FILES = {FILE_DEFAULT};

    public static void addLog(Context context, String content) {
        addLog(context, FILE_DEFAULT, content);
    }

    public static void addLog(Context context, String tag, String content) {

        String logFileName = FILE_DEFAULT;
        Log.d(tag, content);

        if (context == null)
            return;


        if (enable) {
            String timeNow = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss", Locale.US).
                    format(new Date(System.currentTimeMillis()));


            (new File(context.getExternalFilesDir(null) + "/log", "")).mkdirs();
            File file = new File(context.getExternalFilesDir(null) + "/log", logFileName);
            BufferedWriter out;
            try {
                out = new BufferedWriter(new FileWriter(file, true));
                out.write(timeNow + " " + content);
                out.newLine();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "addLog - logger is offline");
        }
    }

    public static void removeLogFile(Context context, String fileName) {

        if (context == null)
            return;

        if (enable) {
            File file = new File(context.getExternalFilesDir(null) + "/log", fileName);
            file.delete();
        } else {
            Log.d(TAG, "removeLogFile - logger is offline");
        }
    }

    public static void removeAllLogFiles(Context context) {

        if (context == null)
            return;

        if (enable) {
            for (int i = 0; i < ALL_FILES.length; i++) {
                removeLogFile(context, ALL_FILES[i]);
            }
            (new File(context.getFilesDir() + "/log", "")).delete();
        } else {
            Log.d(TAG, "removeAllLogs - logger is offline");
        }
    }
}
