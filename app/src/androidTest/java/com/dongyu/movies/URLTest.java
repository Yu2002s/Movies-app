package com.dongyu.movies;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    @Test
    public void testJsoup() throws IOException {
        Document document = Jsoup.connect("https://dianyi.ng")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .header("Cookie", "60b27e2f79149581c86727987ceaab5a=49a5553b38d3af2a02ff798ab85d5459")
                .get();

        System.out.println(document);
    }

}
