package com.mxtech;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.File;

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
    public static void init(String dir, Callback callback) {
        s_callback = callback;
        File file = new File(dir);
        file.mkdirs();
        nativeInitClass(dir);

        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files = file.listFiles();
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
                        onNativeCrash(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    @Keep
    private static void onNativeCrash(String log) {
        NCException ncException = new NCException(log, Thread.currentThread().getStackTrace());
        s_callback.onNativeCrash(ncException);
    }

    private static native void nativeInitClass(String dir);
}
