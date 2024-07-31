package com.dongyu.movies;

import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.IOUtils;

public class MyFileSource implements IDataSource<InputStream> {

    private InputStream inStream;

    public MyFileSource(String filepath) {
        fillStreamFromFile(new File(filepath));
    }

    public MyFileSource(Uri uri) {
        fillStreamFromUri(uri);
    }

    public MyFileSource(File file) {
        fillStreamFromFile(file);
    }

    public MyFileSource(InputStream stream) {
        this.inStream = stream;
    }

    public void fillStreamFromFile(File file) {
        try {
            inStream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void fillStreamFromUri(Uri uri) {
        String scheme = uri.getScheme();
        if (SCHEME_HTTP_TAG.equalsIgnoreCase(scheme) || SCHEME_HTTPS_TAG.equalsIgnoreCase(scheme)) {
            fillStreamFromHttpFile(uri);
        } else if (SCHEME_FILE_TAG.equalsIgnoreCase(scheme)) {
            fillStreamFromFile(new File(uri.getPath()));
        }
    }

    public void fillStreamFromHttpFile(Uri uri) {
        try {
            URL url = new URL(uri.toString());
            url.openConnection();
            inStream = new BufferedInputStream(url.openStream());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void release() {
        IOUtils.closeQuietly(inStream);
        inStream = null;
    }

    @Override
    public InputStream data() {
        return inStream;
    }
}