package com.dongyu.movies;

import android.util.Log;

import com.dongyu.movies.network.Repository;
import com.dongyu.movies.utils.AESUtils;
import com.dongyu.movies.utils.IpUtil;
import com.dongyu.movies.utils.Md5Utils;

import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XmTest {

    @Test
    public void test() {
        String url = URLEncoder.encode( "https://v.qq.com/x/cover/mzc00200yxnj6nj/n41009m0vya.html");
        OkHttpClient okHttpClient = Repository.INSTANCE.getOkHttpClient();
        String ip = IpUtil.getRandomChinaIP();
        String time = String.valueOf(System.currentTimeMillis());
        String content = Md5Utils.md5Hex(time + url);
        if (content == null) {

        }
        String encrypt = AESUtils.encrypt("AES/CBC/NoPadding",
                Md5Utils.md5Hex(content), "3cccf88181408f19", content);
        System.out.println("encrypt: " + encrypt);
        if (encrypt == null) {
            return;
        }
        FormBody formBody = new FormBody.Builder()
                .add("wap", "")
                .add("url", url)
                .add("time", time)
                .add("key", encrypt)
                .build();
        Request request = new Request.Builder()
                .url("https://122.228.8.29:4433/xmflv.js")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0")
                .header("Host", "122.228.8.29:4433")
                .header("Origin", "https://jx.xmflv.com")
                .post(formBody)
                .build();
        try(Response response = okHttpClient.newCall(request).execute()) {
            /*System.out.println(response.headers());
            InputStream inputStream = response.body().byteStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str = "123";
            StringBuilder builder  = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                builder.append(str);
            }

            System.out.println("data: " + builder);*/
             assert response.body() != null;
            String body = response.body().string();
            System.out.println("body: " + body);
            JSONObject jsonObject = new JSONObject(body);
            String encryptUrl = jsonObject.getString("url");
            String key = jsonObject.getString("aes_key");
            String iv = jsonObject.getString("aes_iv");

            System.out.println("encrypt: " + encryptUrl);

            System.out.println("key: " + key);
            System.out.println("iv: " + iv);

            String decrypt = AESUtils.decrypt("AES/CBC/PKCS5Padding", key, iv, encryptUrl);

            System.out.println("decrypt: " + decrypt);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
