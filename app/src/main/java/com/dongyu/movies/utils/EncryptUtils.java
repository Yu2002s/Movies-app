package com.dongyu.movies.utils;

public class EncryptUtils {

    static {
        System.loadLibrary("dyplayer");
    }

    private static EncryptUtils utils = null;

    public static EncryptUtils getInstance() {
        synchronized (EncryptUtils.class) {
            if (utils == null) {
                utils = new EncryptUtils();
            }
            return utils;
        }
    }

    public native String encode(String str);
    public native String decode(String str);
    public native boolean init();
}