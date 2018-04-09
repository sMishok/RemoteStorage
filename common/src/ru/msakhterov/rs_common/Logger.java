package ru.msakhterov.rs_common;

import java.text.SimpleDateFormat;

public class Logger {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yy HH:mm:ss - ");

    public static void putLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) + Thread.currentThread().getName() + ": " + msg;
        System.out.println(msg);
    }
}
