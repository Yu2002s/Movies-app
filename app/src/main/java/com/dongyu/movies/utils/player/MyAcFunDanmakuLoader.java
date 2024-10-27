package com.dongyu.movies.utils.player;

import android.net.Uri;

import java.io.InputStream;
import java.net.URL;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

public class MyAcFunDanmakuLoader implements ILoader {
    private MyAcFunDanmakuLoader(){}
    private static volatile MyAcFunDanmakuLoader instance;
    private JSONSource dataSource;

    public static ILoader instance() {
        if(instance == null){
            synchronized (MyAcFunDanmakuLoader.class){
                if(instance == null)
                    instance = new MyAcFunDanmakuLoader();
            }
        }
        return instance;
    }

    @Override
    public JSONSource getDataSource() {
        return dataSource;
    }

    @Override
    public void load(String json) throws IllegalDataException {
        try {
            dataSource = new JSONSource(json);
        } catch (Exception e) {
            throw new IllegalDataException(e);
        }
    }

    @Override
    public void load(InputStream in) throws IllegalDataException {
        try {
            dataSource = new JSONSource(in);
        } catch (Exception e) {
            throw new IllegalDataException(e);
        }
    }


}