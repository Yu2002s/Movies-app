package com.dongyu.movies.parser;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.ClassifyQueryParam;
import com.dongyu.movies.model.home.FilterData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieVideo;
import com.dongyu.movies.model.parser.ParserResult;
import com.dongyu.movies.model.parser.PlayParam;
import com.dongyu.movies.model.search.SearchData;
import com.dongyu.movies.utils.IpUtil;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    /**
     * 需要返回的格式
     */
    public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";

    /**
     * 语言相关
     */
    public static final String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7";

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

    /**
     * 验证码参数
     */
    public static final String PARAM_VERIFY_CODE = "verify_code";

    /**
     * 验证地址参数
     */
    public static final String PARAM_VERIFY_URL = "verify_url";

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
    private Map<String, Object> params = new ArrayMap<>();

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

    protected BaseParser<T> setParseUrl(String url) {
        this.parseUrl = url;
        URL domain;
        try {
            domain = new URL(parseUrl);
        } catch (MalformedURLException e) {
            return this;
        }
        // 这里获取网站主机信息
        this.host = domain.getProtocol() + "://" + domain.getHost();
        if (domain.getPort() != -1) {
            this.host += ":" + domain.getPort();
        }
        return this;
    }

    protected BaseParser<T> setType(TYPE type) {
        this.type = type;
        return this;
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
        CLASSIFY,
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

    public final String getSelectionId() {
        return getStringParam(PARAM_SELECTION);
    }

    public BaseParser<T> setSelectionId(String selection) {
        return setParam(PARAM_SELECTION, selection);
    }

    public BaseParser<T> setVerifyCode(String code) {
        return setParam(PARAM_VERIFY_CODE, code);
    }

    @Nullable
    public final String getVerifyCode() {
        return getStringParamOrNull(PARAM_VERIFY_CODE);
    }

    public BaseParser<T> setVerifyUrl(String verifyUrl) {
        return setParam(PARAM_VERIFY_URL, verifyUrl);
    }

    @Nullable
    public final String getVerifyUrl() {
        return getStringParamOrNull(PARAM_VERIFY_URL);
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

    @Nullable
    public final String getStringParamOrNull(String key) {
        Object param = getParam(key);
        if (param == null) {
            return null;
        }
        return getStringParam(key);
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
                // TODO: 2024/10/18 暂时将详情和播放一起解析（可能有问题）
            case VIDEO:
                return getParseDetailUrl(url, getDetailId(), getSourceId(), getSelectionId());
            // 分类页单独处理
            case CLASSIFY:
                return getParseClassifyUrl(url, classifyQueryParam);
        }
        // 其它类型的话就返回原来的地址
        return url;
    }

    private String parseUrlForParams(String url) {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("需要解析的地址为空");
        }
        if (params == null || params.isEmpty()) {
            return url;
        }
        String parsedUrl = url;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = "{" + entry.getKey() + "}";
            String value;
            Object valuePram = entry.getValue();
            // 把null转换为空字符串（可能时多余操作，预防错误）
            if (valuePram == null) {
                value = "";
            } else if (valuePram instanceof String) {
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
        return parseUrlForParams(url);
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
        return parseUrlForParams(url);
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
            String sourceId = matcher.group(2);
            String selectionId = matcher.group(3);
            assert detailId != null : "detailId";
            assert sourceId != null : "sourceId";
            assert selectionId != null : "selectionId";
            return new PlayParam(detailId, sourceId, selectionId);
        }
        return null;
    }

    /**
     * 通过评分获取排序id
     *
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
    public ParserResult<SearchData> getSearchList() {
        if (!verify()) {
            return ParserResult.error("验证失败");
        }
        Document document = getDocument(TYPE.SEARCH);
        return document == null ? ParserResult.error(null) : parseSearchList(document);
    }

    public abstract ParserResult<SearchData> parseSearchList(Document document);

    @Override
    public ParserResult<MovieDetail> getDetail() {
        Document document = getDocument(TYPE.DETAIL);
        return document == null ? ParserResult.error(null) : parseDetail(document);
    }

    public abstract ParserResult<MovieDetail> parseDetail(Document document);

    @Override
    public ParserResult<MovieVideo> getVideo() {
        Document document = getDocument(TYPE.VIDEO);
        return document == null ? ParserResult.error(null) : parseVideo(document);
    }

    public abstract ParserResult<MovieVideo> parseVideo(Document document);

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
     * 验证码验证
     */
    public boolean verify() {
        String verifyCode = getVerifyCode();
        String verifyUrl = getVerifyUrl();
        Log.d(TAG, "verifyCode: " + verifyCode + ", verifyUrl: " + verifyUrl);
        if (!TextUtils.isEmpty(verifyCode) && !TextUtils.isEmpty(verifyUrl)) {
            // 需要多余的操作进行验证
            String reqUrl = (host + verifyUrl).replace("{" + PARAM_VERIFY_CODE + "}", verifyCode);
            Log.d(TAG, "reqUrl: " + reqUrl);
            return onVerify(getResponseBody(reqUrl));
        }
        return true;
    }

    public boolean onVerify(String result) {
        Log.d(TAG, "onVerify: " + result);
        return result != null;
    }

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

    @Nullable
    public String getResponseBody(String parseUrl) {
        try {
            Connection.Response response = createSimpleConnection(parseUrl).execute();
            String cookie = getCookieForMap(response.cookies());
            setConnectionCookie(cookie);
            return response.body();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取网站的文档结构对象，用于解析
     *
     * @param parseUrl 解析地址
     * @return 浏览器Document
     */
    public Document getDocument(String parseUrl) {
        try {
            URL domain = new URL(parseUrl);
            // 这里获取网站主机信息
            this.host = domain.getProtocol() + "://" + domain.getHost();
            if (domain.getPort() != -1) {
                this.host += ":" + domain.getPort();
            }
            Log.i(TAG, "getDocument: " + parseUrl);
            Connection connection = createConnection(parseUrl);
            Document document = connection.get();
            Connection.Response response = connection.response();
            String cookie = getCookieForMap(response.cookies());
            setConnectionCookie(cookie);
            return document;
        } catch (IOException e) {
            if (e instanceof HttpStatusException || e instanceof SocketException) {
                String html = WebViewTool.INSTANCE.load(parseUrl);
                if (html == null) {
                    return null;
                }
                return Jsoup.parse(html);
            }
            Log.e(TAG, "getDocument:" + e);
            return null;
        }
    }

    /**
     * 获取当前连接的cookie数据
     *
     * @return cookie
     */
    private String getConnectionCookie() {
        return CookieManager.getInstance().getCookie(host);
    }

    /**
     * 携带cookie到请求头中
     *
     * @param connection 连接对象
     * @param cookie     完整cookie数据
     */
    protected void setConnectionCookie(Connection connection, String cookie) {
        if (TextUtils.isEmpty(cookie)) {
            return;
        }
        Log.d(TAG, this.host + " getCookie: " + cookie);
        connection.header("Cookie", cookie);
    }

    /**
     * 保存Cookie
     *
     * @param cookie 需要保存的cookie数据
     */
    private void setConnectionCookie(String cookie) {
        if (TextUtils.isEmpty(cookie)) {
            return;
        }
        Log.d(TAG, host + " saveCookie: " + cookie);
        CookieManager.getInstance().setCookie(host, cookie);
    }

    /**
     * 通过cookieMap获取cookie信息
     *
     * @param cookieMap cookie表
     * @return 完整cookie信息
     */
    protected String getCookieForMap(Map<String, String> cookieMap) {
        if (cookieMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            if (i != cookieMap.size() - 1) {
                builder.append("; ");
            }
            i++;
        }
        return builder.toString();
    }

    /**
     * 构建普通连接
     *
     * @param url 加载地址
     * @return 连接对象
     */
    public Connection createSimpleConnection(String url) {
        return createConnection(url).ignoreContentType(true);
    }

    /**
     * 通过url获取响应的字节数据
     *
     * @param url 请求地址
     * @return 字节数据
     */
    @Nullable
    public byte[] getResponseBytes(String url)  {
        try {
            return createSimpleConnection(url).execute().bodyAsBytes();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建链接，对一些必要的请求头参数有进行设置
     *
     * @param parseUrl 已解析地址
     * @return 连接对象
     */
    protected Connection createConnection(String parseUrl) {
        Connection connection = Jsoup.connect(parseUrl);
        String cookie = getConnectionCookie();
        setConnectionCookie(connection, cookie);
        addConnectionHeaders(connection);
        connection.timeout(MAX_TIMEOUT);
        return connection;
    }

    protected void addConnectionHeaders(Connection connection) {
        String ip = IpUtil.getRandomChinaIP();
        connection.userAgent(USER_AGENT);
        connection.header("Accept", ACCEPT);
        connection.header("Accept-Language", ACCEPT_LANGUAGE);
        connection.header("X-Forwarded-For", ip);
        connection.header("HTTP_X_FORWARDED_FOR", ip);
        connection.header("HTTP_CLIENT_IP", ip);
        connection.header("REMOTE_ADDR", ip);
        connection.header("X-Real-IP", ip);
        connection.header("X-Originating-IP", ip);
        connection.header("Proxy-Client-IP", ip);
        connection.header("X-Remote-IP", ip);
        connection.header("WL-Proxy-Client-IP", ip);
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
