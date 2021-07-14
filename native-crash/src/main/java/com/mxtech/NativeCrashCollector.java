package com.mxtech;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class NativeCrashCollector {
    static {
        System.loadLibrary("mx-nc");
    }

    public interface Callback {
        void onNativeCrash(@NonNull Exception exception);
    }

    private static Callback s_callback = null;
    private static File s_crashDir = null;
    private static Executor s_executor = null;

    public static void init(Context context, Executor executor, Callback callback) {
        if (executor == null) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            threadPoolExecutor.setKeepAliveTime(1, TimeUnit.SECONDS);
            threadPoolExecutor.allowCoreThreadTimeOut(true);

            executor = threadPoolExecutor;
        }

        s_executor = executor;
        s_callback = callback;
        s_crashDir = new File(context.getExternalCacheDir(), "nc");

        s_crashDir.mkdirs();
        nativeInitClass(s_crashDir.getAbsolutePath());
    }

    public static void startReport() {
        s_executor.execute(new Runnable() {
            @Override
            public void run() {
                listNativeCrash();
                listNativeAndJavaCrash();
            }
        });
    }

    private static void listNativeCrash() {
        File[] files = s_crashDir.listFiles();
        if (files == null)
            return;

        for (File file1 : files) {
            String name = file1.getName();
            if (!name.startsWith("nc_") || !name.endsWith(".txt")) {
                continue;
            }

            try {
                Source source = Okio.source(file1);
                BufferedSource buffer = Okio.buffer(source);
                byte[] bytes = buffer.readByteArray();
                source.close();
                file1.delete();
                String s = new String(bytes);
                NCException ncException = new NCException(s, null);
                callback(ncException);
//                        onNativeCrash(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static void listNativeAndJavaCrash() {
        File dir = new File(s_crashDir, "nc_java");
        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file1 : files) {
            String name = file1.getName();
            if (!name.startsWith("nc_") || !name.endsWith(".txt")) {
                continue;
            }

            try {
                NCException ncException = NCException.createFromFile(file1);
                if (ncException != null) {
                    s_callback.onNativeCrash(ncException);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            file1.delete();

        }
    }

    @Keep
    private static void onNativeCrash(String log) {
        NCException ncException = new NCException(log, Thread.currentThread().getStackTrace());

        File dir = new File(s_crashDir, "nc_java");
        dir.mkdirs();
        long now = System.currentTimeMillis();
        String fileName = "nc_" + now + ".txt";
        File file = new File(dir, fileName);
        try {
            ncException.writeToFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        s_callback.onNativeCrash(ncException);
    }

    private static void callback(NCException e) {
        s_callback.onNativeCrash(e);
    }


    private static native void nativeInitClass(String dir);
}
