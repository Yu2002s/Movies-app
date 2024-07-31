package com.dongyu.movies;

import android.net.Uri;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class URLTest {

    @Test
    public void test() throws IOException {
        Uri uri = Uri.parse("https://danmu.zxz.ee/?type=xml&id=59056300aa9416f4470038223a374993");
        System.out.println(uri.getPath());
        URL url = new URL(uri.getPath());
        url.openConnection();
    }

}
