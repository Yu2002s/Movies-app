package com.dongyu.movies.parser;

import android.util.Log;

import androidx.annotation.Nullable;

import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieVideo;
import com.dongyu.movies.model.parser.ParserResult;
import com.dongyu.movies.model.search.SearchData;
import com.dongyu.movies.network.Repository;
import com.dongyu.movies.utils.AESUtils;
import com.dongyu.movies.utils.IpUtil;
import com.dongyu.movies.utils.Md5Utils;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.net.URLDecoder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 通用解析器
 */
public class SimpleParser extends BaseParser<Object> {

    private static final Pattern ID_PATTERN = Pattern.compile("([0-9a-zA-Z]+)\\.html");

    /**
     * 虾米解析接口
     */
    private static final String XM_API = "https://122.228.8.29:4433/xmflv.js";

    /**
     * 虾米AES加密偏移量
     */
    private static final String XM_AES_IV = "3cccf88181408f19";

    /**
     * xbq解析接口
     */
    private static final String XBQ_API = "https://jxapi.xbiqu5.com/CloudApi.php";

    @Override
    public ParserResult<SearchData> parseSearchList(Document document) {
        return null;
    }

    @Override
    public ParserResult<MovieDetail> parseDetail(Document document) {
        return null;
    }

    @Override
    public ParserResult<MovieVideo> parseVideo(Document document) {
        return null;
    }

    @Override
    protected ParserResult<MainData> parseMain(Document document) {
        return null;
    }

    @Override
    public ParserResult<CategoryData> parseClassify(Document document, boolean notParams) {
        return null;
    }

    /**
     * 通过href获取id
     * @param href 链接
     * @return id
     */
    public final String getIdForHref(String href) {
        Matcher matcher = ID_PATTERN.matcher(href);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static Request.Builder addIPHeaders(Request.Builder builder) {
        String ip = IpUtil.getRandomChinaIP();
        return builder.header("User-Agent", USER_AGENT)
                .header("X-Forwarded-For", ip)
                .header("HTTP_X_FORWARDED_FOR", ip)
                .header("HTTP_CLIENT_IP", ip)
                .header("REMOTE_ADDR", ip)
                .header("X-Real-IP", ip)
                .header("X-Originating-IP", ip)
                .header("Proxy-Client-IP", ip)
                .header("X-Remote-IP", ip)
                .header("WL-Proxy-Client-IP", ip);
    }

    /**
     * 通用视频链接解析
     * @return 视频播放地址
     */
    public static String parseVideoUrl(String url) {
        // 虾米解析播放器
        String videoUrl = parseXm(url);
        if (videoUrl == null) {
            // XMFLV解析播放器
            videoUrl = parseXMFLV(url);
        }
        Log.d(TAG, "parseVideoUrl: " + videoUrl);
        return videoUrl;
    }

    /**
     * 虾米解析播放地址（必须是官方地址）
     *
     * @param url 地址（官方播放地址）
     * @return m3u8播放地址
     */
    @Nullable
    public static String parseXm(String url) {
        OkHttpClient okHttpClient = Repository.INSTANCE.getOkHttpClient();
        String time = String.valueOf(System.currentTimeMillis());
        String content = Md5Utils.md5Hex(time + url);
        if (content == null) {
            return null;
        }
        String key = Md5Utils.md5Hex(content);
        if (key == null) {
            return null;
        }
        String encrypt = AESUtils.encrypt("AES/CBC/NoPadding",
                key, XM_AES_IV, content);
        if (encrypt == null) {
            return null;
        }
        FormBody formBody = new FormBody.Builder()
                .add("wap", "")
                .add("url", url)
                .add("time", time)
                .add("key", encrypt)
                .build();
        Request request = addIPHeaders(new Request.Builder())
                .url(XM_API)
                .header("User-Agent", USER_AGENT)
                .header("Host", "122.228.8.29:4433")
                .header("Origin", "https://jx.xmflv.com")
                .post(formBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            assert response.body() != null;
            String body = response.body().string();
            Log.d(TAG, "xm-body: " + body);
            JSONObject jsonObject = new JSONObject(body);
            int code = jsonObject.getInt("code");
            if (code != 200) {
                return null;
            }
            String encryptUrl = jsonObject.getString("url");
            String aes_key = jsonObject.getString("aes_key");
            String iv = jsonObject.getString("aes_iv");
            return AESUtils.decrypt("AES/CBC/PKCS5Padding", aes_key, iv, encryptUrl);
        } catch (Exception e) {
            Log.e(TAG, "parseXm: " + e);
            return null;
        }
    }

    /**
     * XMFLV 解析播放器
     *
     * @param url 官方播放地址
     * @return 视频地址
     */
    @Nullable
    public static String parseXMFLV(String url) {
        String t = String.valueOf(System.currentTimeMillis() / 1000);
        OkHttpClient okHttpClient = Repository.INSTANCE.getOkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("url", url)
                .add("t", t)
                .add("token", Objects.requireNonNull(Md5Utils.md5Hex(url + t)))
                .build();
        Request request = addIPHeaders(new Request.Builder())
                .url(XBQ_API)
                .post(formBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            assert body != null;
            String responseBody = body.string();
            JSONObject jsonObject = new JSONObject(responseBody);
            int code = jsonObject.getInt("code");
            if (code != 200) {
                return null;
            }
            return URLDecoder.decode(jsonObject.getString("url"));
        } catch (Exception e) {
            Log.e(TAG, "parseXbq: " + e);
            return null;
        }
    }
}
