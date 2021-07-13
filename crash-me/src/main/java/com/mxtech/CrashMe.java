package com.mxtech;

import android.os.Handler;
import android.os.Looper;

public class CrashMe {
    static {
        System.loadLibrary("mx-crash-me");
    }

    public static int CRASH_NPE = 1;
    public static int CRASH_STACK_OVER_FLOW = 2;

    public static void crash(int type, boolean mainThread, boolean javaThread, int delay) {
        if (mainThread) {
            Looper mainLooper = Looper.getMainLooper();
            Thread thread = mainLooper.getThread();
            if (Thread.currentThread().getId() != thread.getId()) {
                throw new RuntimeException("must called in main thread.");
            }

            new Handler(mainLooper).postDelayed(new Runnable() {
                @Override
                public void run() {
                    crash(type, false);
                }
            }, delay);

            return;
        }

        if (javaThread) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    crash(type, false);
                }
            }).start();
            return;
        }

        Looper mainLooper = Looper.getMainLooper();

        new Handler(mainLooper).postDelayed(new Runnable() {
            @Override
            public void run() {
                crash(type, true);
            }
        }, delay);
    }

    private static native void crash(int type, boolean thread);
}
