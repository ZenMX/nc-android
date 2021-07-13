package com.mxtech;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

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
    private static File crashDir = null;

    public static void init(String dir, Callback callback) {
        s_callback = callback;
        crashDir = new File(dir);
        crashDir.mkdirs();
        nativeInitClass(dir);

        new Thread(new Runnable() {
            @Override
            public void run() {
                listNativeCrash();
                listNativeAndJavaCrash();
            }
        }).start();
    }

    private static void listNativeCrash() {
        File[] files = crashDir.listFiles();
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
                NCException ncException = createNcException(s);
                callback(ncException);
//                        onNativeCrash(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static void listNativeAndJavaCrash() {
        File dir = new File(crashDir, "nc_java");
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
        NCException ncException = createNcException(log);

        File dir = new File(crashDir, "nc_java");
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
    @NonNull
    private static NCException createNcException(String log) {
        return new NCException(log, Thread.currentThread().getStackTrace());
    }





    private static native void nativeInitClass(String dir);
}
