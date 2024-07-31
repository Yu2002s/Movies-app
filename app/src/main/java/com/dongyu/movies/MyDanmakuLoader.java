package com.dongyu.movies;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.BiliDanmakuLoader;
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource;

public class MyDanmakuLoader implements ILoader {

    private static MyDanmakuLoader _instance;

    private MyFileSource dataSource;

    private MyDanmakuLoader() {

    }

    public static MyDanmakuLoader instance() {
        if (_instance == null) {
            _instance = new MyDanmakuLoader();
        }
        return _instance;
    }

    public void load(String uri) throws IllegalDataException {
        try {
            dataSource = new MyFileSource(Uri.parse(uri));
        } catch (Exception e) {
            throw new IllegalDataException(e);
        }
    }

    public void load(InputStream stream) {
        dataSource = new MyFileSource(stream);
    }

    @Override
    public MyFileSource getDataSource() {
        return dataSource;
    }

}
