package com.mxtech;

import androidx.annotation.NonNull;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;

public class NCException extends Exception {

    private final StackTraceElement[] traceElements;
    private String msg;

    public NCException(String nativeStack, StackTraceElement[] traceElements) {
        super();
        LinkedList<StackTraceElement> list = new LinkedList<>();
        String[] split = nativeStack.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                msg = split[i];
            } else {
                String str = split[i];
                String[] splitLine = str.split(":");
                if (splitLine.length == 3) {
                    String lib = splitLine[0];
                    String function = splitLine[1];
                    String address = splitLine[2];
                    StackTraceElement stackTraceElement = new StackTraceElement(lib, function + ':' + address, null, -2);
                    list.add(stackTraceElement);
                }
            }

        }

        if (traceElements != null) {
            list.addAll(Arrays.asList(traceElements));
        }

        this.traceElements = list.toArray(new StackTraceElement[0]);
    }


    @NonNull
    @Override
    public StackTraceElement[] getStackTrace() {
        return traceElements;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public void printStackTrace(@NonNull PrintStream printStream) {
        synchronized (printStream) {
            String s = this.toString();
            printStream.println(s + ": " + msg);
//            printStream.println(msg);
//            for (String str: split) {
//                printStream.println("\tat " + str);
//            }


            for (StackTraceElement traceElement : traceElements) {
                printStream.println("\tat " + traceElement);
            }
            printStream.println();
        }
    }

}
