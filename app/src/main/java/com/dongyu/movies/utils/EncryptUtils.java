package com.dongyu.movies.utils;

/**
 * 加密工具类
 */
public class EncryptUtils {

    static {
        // 加载本地库，用于加密解密，校验App签名
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