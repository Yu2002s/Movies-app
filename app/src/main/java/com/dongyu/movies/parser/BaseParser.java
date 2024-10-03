package com.dongyu.movies.parser;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.ClassifyQueryParam;
import com.dongyu.movies.model.home.FilterData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieItem;
import com.dongyu.movies.model.page.PageResult;
import com.dongyu.movies.model.parser.ParserResult;
import com.dongyu.movies.model.parser.PlayParam;
import com.dongyu.movies.network.Repository;
import com.dongyu.movies.utils.AESUtils;
import com.dongyu.movies.utils.IpUtil;
import com.dongyu.movies.utils.Md5Utils;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 基础解析器
 */
public abstract class BaseParser<T> implements HtmlParseable<T> {

    public static final String TAG = BaseParser.class.getSimpleName();

    /**
     * 解析最大超时时间
     */
    private static final int MAX_TIMEOUT = 18 * 1000;

    /**
     * 用户代理
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    /**
     * 需要返回的格式
     */
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

    /**
     * 语言相关
     */
    private static final String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7";

    /**
     * 虾米解析接口
     */
    private static final String XM_API = "https://122.228.8.29:4433/xmflv.js";

    /**
     * 虾米AES加密偏移量
     */
    private static final String XM_AES_IV = "3cccf88181408f19";

    /**
     * 名称参数(搜索需要)
     */
    public static final String PARAM_NAME = "name";

    /**
     * 页码参数（分页需要）
     */
    public static final String PARAM_PAGE = "page";

    /**
     * 详情id参数
     */
    public static final String PARAM_DETAIL_ID = "detailId";

    /**
     * 线路id参数（跳转详情页需要）
     */
    public static final String PARAM_ROUTE_ID = "sourceId";

    /**
     * 集数参数（详情页需要）
     */
    public static final String PARAM_SELECTION = "selectionId";

    public static final String[] filterTypes = new String[]{
            FilterData.FILTER_TYPE,
            FilterData.FILTER_AREA,
            FilterData.FILTER_LANGUAGE,
            FilterData.FILTER_LETTER,
            FilterData.FILTER_YEAR,
            FilterData.FILTER_SORT
    };

    public static Map<String, String> filterMap = new HashMap<>();

    static {
        filterMap.put("剧情", FilterData.FILTER_TYPE);
        filterMap.put("类型", FilterData.FILTER_CATE);
        filterMap.put("地区", FilterData.FILTER_AREA);
        filterMap.put("语言", FilterData.FILTER_LANGUAGE);
        filterMap.put("年份", FilterData.FILTER_YEAR);
        filterMap.put("字母", FilterData.FILTER_LETTER);
        filterMap.put("排序", FilterData.FILTER_SORT);
    }

    /**
     * 网站地址（未解析的地址）
     */
    private String url;

    /**
     * 网站主机地址
     */
    private String host;

    /**
     * 实际解析地址
     */
    private String parseUrl;

    /**
     * 解析地址所需的一些参数
     */
    private Map<String, Object> params;

    /**
     * 分类筛选参数
     */
    private ClassifyQueryParam classifyQueryParam;

    /**
     * 解析类型
     */
    private TYPE type;

    /**
     * 获取网站地址（未进行解析的地址）
     *
     * @return 网站地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取网站主机地址
     *
     * @return 主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取解析地址
     *
     * @return 已解析的地址
     */
    public String getParseUrl() {
        return parseUrl;
    }

    /**
     * 获取解析id，默认需要子类进行实现
     *
     * @return 解析器id
     */
    public int getParseId() {
        return -1;
    }

    /**
     * 获取所有已设置的参数
     *
     * @return 参数集合
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 获取设置的分类参数
     *
     * @return ClassifyQueryParam
     */
    public ClassifyQueryParam getClassifyQueryParam() {
        return classifyQueryParam;
    }

    /**
     * 获取解析类型
     *
     * @return @see Type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * 解析类型定义
     */
    public enum TYPE {
        /**
         * 普通解析
         */
        NORMAL,
        /**
         * 搜索页
         */
        SEARCH,
        /**
         * 详情页
         */
        DETAIL,
        /**
         * 视频播放地址
         */
        VIDEO,
        /**
         * 主页
         */
        MAIN,
        /**
         * 分类页
         */
        CLASSIFY
    }

    /**
     * 设置需要解析的网站地址（必要参数）
     *
     * @param url 网站地址
     * @return BaseParser
     */
    public BaseParser<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 直接使用一个Map集合设置参数（会直接覆盖原有参数）
     *
     * @param params 参数
     * @return BaseParser
     */
    public BaseParser<T> setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * 设置分类所需的参数，是一个Map集合
     *
     * @param classifyQueryParam 参数
     * @return BaseParser
     */
    public BaseParser<T> setClassifyQueryParam(ClassifyQueryParam classifyQueryParam) {
        this.classifyQueryParam = classifyQueryParam;
        return this;
    }

    /**
     * 设置自定义参数
     *
     * @param key   键
     * @param value 值
     * @return BaseParser
     */
    public final BaseParser<T> setParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    /**
     * 设置播放所需的一些参数
     *
     * @param param 播放参数
     * @return BasePlayer
     */
    public final BaseParser<T> setPlayParam(PlayParam param) {
        setDetailId(param.getDetailId());
        setSourceId(param.getSourceId());
        setSelectionId(param.getSelectionId());
        setUrl(param.getVideoUrl());
        return this;
    }

    /**
     * 获取设置的页码参数
     *
     * @return page页码
     */
    public final int getPage() {
        return getIntegerParam(PARAM_PAGE);
    }

    /**
     * 设置页码信息
     *
     * @param page 页码
     * @return BaseParser
     */
    public BaseParser<T> setPage(int page) {
        return setParam(PARAM_PAGE, page);
    }

    public final String getName() {
        return getStringParam(PARAM_NAME);
    }

    public BaseParser<T> setName(String name) {
        return setParam(PARAM_NAME, name);
    }

    /**
     * 获取详情id
     *
     * @return 详情页id
     */
    public final String getDetailId() {
        return getStringParam(PARAM_DETAIL_ID);
    }

    public BaseParser<T> setDetailId(String id) {
        return setParam(PARAM_DETAIL_ID, id);
    }

    public final String getSourceId() {
        return getStringParam(PARAM_ROUTE_ID);
    }

    public BaseParser<T> setSourceId(String routeId) {
        return setParam(PARAM_ROUTE_ID, routeId);
    }

    public final String getSelection() {
        return getStringParam(PARAM_SELECTION);
    }

    public BaseParser<T> setSelectionId(String selection) {
        return setParam(PARAM_SELECTION, selection);
    }

    @Nullable
    public final Object getParam(String key) {
        return params.get(key);
    }

    public final Integer getIntegerParam(String key) {
        return (Integer) getParam(key);
    }

    public final String getStringParam(String key) {
        return (String) getParam(key);
    }

    /**
     * 通过未解析的地址和解析类型，获取到解析之后的地址
     *
     * @param url  未解析的网站地址
     * @param type 解析类型
     * @return 解析后的地址
     */
    protected String getParseUrl(String url, TYPE type) {
        switch (type) {
            // 搜索类型: 需要搜索名称和页码参数
            case SEARCH:
                return getSearchListUrl(url, getName(), getPage());
            case DETAIL:
                // TODO: 2024/9/7 需要将详情页和播放地址解析分开
            case VIDEO:
                return getParseDetailUrl(url, getDetailId(), getSourceId(), getSelection());
            // 分类页单独处理
            case CLASSIFY:
                return getParseClassifyUrl(url, classifyQueryParam);
        }
        // 其它类型的话就返回原来的地址
        return url;
    }

    private String parseUrlForParams() {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("需要解析的地址为空");
        }
        String parsedUrl = url;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            String value;
            Object valuePram = entry.getValue();
            if (valuePram instanceof String) {
                value = (String) valuePram;
            } else {
                value = String.valueOf(valuePram);
            }
            parsedUrl = parsedUrl.replace(key, value);
        }
        return parsedUrl;
    }

    @Nullable
    private String parseUrlForClassifyParam() {
        Class<ClassifyQueryParam> aClass = ClassifyQueryParam.class;
        Field[] fields = aClass.getDeclaredFields();

        String parsedUrl = url;

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object valueParam = field.get(classifyQueryParam);
                String fieldName = field.getName();
                // 判定为其他参数为String类型，直接设置空字符串
                // 后续可能增加字段，这里得做类型判断
                if (valueParam == null) {
                    valueParam = "";
                }
                String key = "{" + fieldName + "}";
                String value;
                if (valueParam instanceof String) {
                    value = (String) valueParam;
                } else {
                    value = String.valueOf(valueParam);
                }
                parsedUrl = parsedUrl.replace(key, value);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseUrlForClassifyParam: " + e);
        }

        return parsedUrl;
    }

    /**
     * 获取搜索列表解析后的地址
     *
     * @param url  未解析的网站地址
     * @param name 搜索关键字
     * @param page 页码
     * @return 解析后的地址
     */
    @Nullable
    public String getSearchListUrl(String url, String name, Integer page) {
        // https://localhost:21/vod/play/{detailId}/{page}/{selection}
        return parseUrlForParams();
    }

    /**
     * 获取详情页解析后的地址
     *
     * @param url       未解析地址
     * @param detailId  详情id
     * @param routeId   线路id
     * @param selection 集数
     * @return 解析后地址
     */
    @Nullable
    public String getParseDetailUrl(String url, String detailId, String routeId, String selection) {
        return parseUrlForParams();
    }

    /**
     * 获取分类页的解析地址（必须实现）
     *
     * @param url   未解析地址
     * @param param 分类过滤参数
     * @return 解析后的地址
     */
    public String getParseClassifyUrl(String url, ClassifyQueryParam param) {
        return parseUrlForClassifyParam();
    }

    public String getFilterType(String name) {
        return filterMap.getOrDefault(name, "");
    }

    /**
     * 通过播放链接解析出播放所需的一些参数
     *
     * @param href 播放地址
     * @return 播放属性
     */
    @Nullable
    public PlayParam getPlayParamForUrl(String href) {
        // /index.php/vod/play/id/106197/sid/1/nid/1.html
        // /vodplay/75319-2-1.html
        // /paly-9MdSCS-1-1.html
        Pattern pattern = Pattern.compile("([0-9a-zA-Z]+)[-/sid]+(\\d+)[-/nid]+(\\d+)\\.html");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            String detailId = matcher.group(1);
            String routeId = matcher.group(2);
            String selectionId = matcher.group(3);
            assert detailId != null : "detailId";
            assert routeId != null : "routeId";
            assert selectionId != null : "selectionId";
            return new PlayParam(detailId, routeId, selectionId);
        }
        return null;
    }

    /**
     * 通过评分获取排序id
     * @param name 名称
     * @return id
     */
    public String getSortIdByName(String name) {
        if (name.contains("人气")) {
            return "hits";
        } else if (name.contains("时间")) {
            return "time";
        } else if (name.contains("评分")) {
            return "score";
        }
        return "";
    }

    @Override
    public ParserResult<T> parse() {
        Document document = getDocument(TYPE.NORMAL);
        return document == null ? ParserResult.error(null) : parse(document);
    }

    public ParserResult<T> parse(Document document) {
        return ParserResult.error(null);
    }

    @Override
    public ParserResult<PageResult<MovieItem>> getSearchList() {
        Document document = getDocument(TYPE.SEARCH);
        return document == null ? ParserResult.error(null) : parseSearchList(document);
    }

    public abstract ParserResult<PageResult<MovieItem>> parseSearchList(Document document);

    @Override
    public ParserResult<MovieDetail> getDetail() {
        Document document = getDocument(TYPE.DETAIL);
        return document == null ? ParserResult.error(null) : parseDetail(document);
    }

    public abstract ParserResult<MovieDetail> parseDetail(Document document);

    @Override
    public ParserResult<String> getVideo() {
        Document document = getDocument(TYPE.VIDEO);
        return document == null ? ParserResult.error(null) : parseVideo(document);
    }

    public abstract ParserResult<String> parseVideo(Document document);

    @Override
    public ParserResult<MainData> getMain() {
        Document document = getDocument(TYPE.MAIN);
        return document == null ? ParserResult.error(null) : parseMain(document);
    }

    protected abstract ParserResult<MainData> parseMain(Document document);

    @Override
    public ParserResult<CategoryData> getClassify() {
        Document document = getDocument(TYPE.CLASSIFY);
        return document == null ? ParserResult.error(null) : parseClassify(document, notClassifyQueryParams());
    }

    public abstract ParserResult<CategoryData> parseClassify(Document document, boolean notParams);

    /**
     * 获取有没有设置分类所需的过滤参数
     *
     * @return 有无设置分类过滤参数
     */
    public boolean notClassifyQueryParams() {
        // boolean emptyCate = TextUtils.isEmpty(classifyQueryParam.getCateId());
        boolean emptyType = TextUtils.isEmpty(classifyQueryParam.getType());
        boolean emptySort = TextUtils.isEmpty(classifyQueryParam.getSort());
        boolean emptyArea = TextUtils.isEmpty(classifyQueryParam.getArea());
        boolean emptyLanguage = TextUtils.isEmpty(classifyQueryParam.getLanguage());
        boolean emptyYear = TextUtils.isEmpty(classifyQueryParam.getYear());
        boolean emptyLetter = TextUtils.isEmpty(classifyQueryParam.getLetter());
        return classifyQueryParam.getPage() == 1 && emptyType && emptySort
                && emptyArea && emptyLanguage && emptyYear && emptyLetter;
    }

    public Document getDocument(TYPE type) {
        this.type = type;
        parseUrl = getParseUrl(url, type);
        return getDocument(parseUrl);
    }

    /**
     * 获取网站的文档结构对象，用于解析
     *
     * @param parserUrl 解析地址
     * @return 浏览器Document
     */
    public Document getDocument(String parserUrl) {
        try {
            Log.i(TAG, "getDocument: " + parserUrl);
            Connection connection = createConnection(parseUrl);
            Document document = connection.get();
            Connection.Response response = connection.response();
            URL domain = response.url();
            // 这里获取网站主机信息
            this.host = domain.getProtocol() + "://" + domain.getHost();
            if (domain.getPort() != -1) {
                this.host += ":" + domain.getPort();
            }
            return document;
        } catch (IOException e) {
            Log.e(TAG, "getDocument:" + e);
            return null;
        }
    }

    /**
     * 构建链接，对一些必要的请求头参数有进行设置
     *
     * @param parseUrl 已解析地址
     * @return 连接对象
     */
    private Connection createConnection(String parseUrl) {
        String ip = IpUtil.getRandomChinaIP();
        Connection connect = Jsoup.connect(parseUrl);
        connect.userAgent(USER_AGENT);
        connect.header("Accept", ACCEPT);
        connect.header("Accept-Language", ACCEPT_LANGUAGE);
        connect.header("X-Forwarded-For", ip);
        connect.header("HTTP_X_FORWARDED_FOR", ip);
        connect.header("HTTP_CLIENT_IP", ip);
        connect.header("REMOTE_ADDR", ip);
        connect.header("X-Real-IP", ip);
        connect.header("X-Originating-IP", ip);
        connect.header("Proxy-Client-IP", ip);
        connect.header("X-Remote-IP", ip);
        connect.header("WL-Proxy-Client-IP", ip);
        connect.timeout(MAX_TIMEOUT);
        return connect;
    }

    /**
     * 虾米解析播放地址（必须是官方地址）
     *
     * @param url 地址（官方播放地址）
     * @return m3u8播放地址
     */
    @Nullable
    public String parseXm(String url) {
        // url = URLEncoder.encode(url);
        OkHttpClient okHttpClient = Repository.INSTANCE.getOkHttpClient();
        String ip = IpUtil.getRandomChinaIP();
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
        Request request = new Request.Builder()
                .url(XM_API)
                .header("User-Agent", USER_AGENT)
                .header("Host", "122.228.8.29:4433")
                .header("Origin", "https://jx.xmflv.com")
                .header("X-Forwarded-For", ip)
                .header("HTTP_X_FORWARDED_FOR", ip)
                .header("HTTP_CLIENT_IP", ip)
                .header("REMOTE_ADDR", ip)
                .header("X-Real-IP", ip)
                .header("X-Originating-IP", ip)
                .header("Proxy-Client-IP", ip)
                .header("X-Remote-IP", ip)
                .header("WL-Proxy-Client-IP", ip)
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

    @NonNull
    public <E> E requireNonNull(@Nullable E obj) {
        return requireNonNull(obj, null);
    }

    @NonNull
    public <E> E requireNonNull(@Nullable E obj, String msg) {
        if (obj == null) {
            throw new ParseException(msg == null ? "解析失败" : msg);
        }
        return obj;
    }

    /**
     * 自定义解析异常
     */
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}
