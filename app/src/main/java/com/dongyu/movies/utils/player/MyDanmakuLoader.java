package com.dongyu.movies.utils.player;

import android.net.Uri;

import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;

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
