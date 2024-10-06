package com.dongyu.movies.parser.impl;

import android.util.Base64;
import android.util.Log;

import com.dongyu.movies.model.home.BannerItem;
import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.FilterData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.home.MoviesCard;
import com.dongyu.movies.model.home.NavItem;
import com.dongyu.movies.model.movie.BaseMovieItem;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieItem;
import com.dongyu.movies.model.movie.VideoSource;
import com.dongyu.movies.model.page.PageResult;
import com.dongyu.movies.model.parser.ParserResult;
import com.dongyu.movies.model.parser.PlayParam;
import com.dongyu.movies.parser.ParserList;
import com.dongyu.movies.parser.SimpleParser;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * mxTheme 对应通用解析器
 */
public class MxThemeParser extends SimpleParser {

    private static final String TAG = MxThemeParser.class.getSimpleName();

    public static Gson gson = new Gson();

    @Override
    public int getParseId() {
        return ParserList.MX_THEME.getParseId();
    }

    public static MxThemeParser create() {
        return new MxThemeParser();
    }

    private List<MovieItem> getSearchListSimple(Elements els) {
        return els.stream().map(element -> {
            String cate = requireNonNull(element.selectFirst(".module-card-item-class")).text();
            MovieItem movieItem = new MovieItem();
            Element base = requireNonNull(element.selectFirst("a.module-card-item-poster"));
            movieItem.setId(parseId(base.attr("href")));
            movieItem.setStatus(requireNonNull(base.selectFirst(".module-item-note")).text());
            String img = requireNonNull(base.selectFirst(".module-item-pic img")).attr("data-original");
            if (!img.startsWith("http")) {
                img = getHost() + img;
            }
            movieItem.setCover(img);

            Element info = requireNonNull(element.selectFirst(".module-card-item-info"));
            String title = requireNonNull(info.selectFirst(".module-card-item-title")).text();
            Elements infoItems = info.select(".module-info-item .module-info-item-content");
            String[] arr = infoItems.get(0).text().split("/");

            movieItem.setTvName(title);
            movieItem.setYears(arr[0]);
            movieItem.setArea(arr[1]);
            movieItem.setType(arr[2]);

            movieItem.setStar(infoItems.get(1).text());

            movieItem.setCate(cate);
            return movieItem;
        }).collect(Collectors.toList());
    }

    private List<MovieItem> getModules(Elements els) {
        List<MovieItem> list = new ArrayList<>();
        for (Element module : els) {
            Elements moduleItems = module.select(".module-items .module-item");
            List<MovieItem> movieItems = moduleItems.stream().map(element -> {
                MovieItem movieItem = new MovieItem();
                String cover = requireNonNull(element.selectFirst(".module-item-pic img"))
                        .attr("data-src");
                if (!cover.startsWith("http")) {
                    cover = getHost() + cover;
                }
                Elements captions = element.select(".module-item-caption span");
                String year = captions.get(0).text();
                String cate = captions.get(1).text();
                String area = captions.get(2).text();
                Element content = requireNonNull(element.selectFirst(".module-item-content"));
                Element videoName = requireNonNull(content.selectFirst(".video-name a"));
                String name = videoName.text();
                String star = requireNonNull(element.selectFirst(".video-tag")).text();
                String status = requireNonNull(element.selectFirst(".module-item-text")).text();
                movieItem.setTvName(name);
                movieItem.setYears(year);
                movieItem.setCate(cate);
                movieItem.setArea(area);
                movieItem.setStar(star);
                movieItem.setStatus(status);
                movieItem.setCover(cover);
                return movieItem;
            }).collect(Collectors.toList());
            list.addAll(movieItems);
        }
        return list;
    }

    private List<MovieItem> getSearchModules(Elements els) {
        return els.stream().map(element -> {
            String cover = requireNonNull(element.selectFirst(".module-item-pic  img"))
                    .attr("data-src");
            if (!cover.startsWith("http")) {
                cover = getHost() + cover;
            }
            Element videoInfo = requireNonNull(element.selectFirst(".video-info"));
            Element header = requireNonNull(videoInfo.selectFirst(".video-info-header"));
            String status = requireNonNull(header.selectFirst(".video-serial")).text();
            Element titleEl = requireNonNull(header.selectFirst("h3"));
            String id = parseId(requireNonNull(titleEl.selectFirst("a")).attr("href"));
            String title = titleEl.text();
            Elements tags = header.select(".tag-link");
            String cate = tags.get(0).text();
            String year = tags.get(1).text();
            String area = tags.get(2).text();
            Elements infos = element.select(".video-info-main .video-info-item");
            String director = infos.get(0).text().replace("/", " ");
            String star = infos.get(1).text().replace("/", " ");
            String detail = infos.get(2).text();

            MovieItem movieItem = new MovieItem();
            movieItem.setCover(cover);
            movieItem.setId(id);
            movieItem.setStar(star);
            movieItem.setTvName(title);
            movieItem.setCate(cate);
            movieItem.setYears(year);
            movieItem.setArea(area);
            movieItem.setDirector(director);
            movieItem.setDetail(detail);
            movieItem.setStatus(status);

            return movieItem;
        }).collect(Collectors.toList());
    }

    @Override
    public ParserResult<PageResult<MovieItem>> parseSearchList(Document document) {
        Element content = requireNonNull(document.selectFirst(".content"));
        Elements searchList = content.select(".module-main .module-items .module-item");

        float searchItem;
        List<MovieItem> list;
        if (searchList.isEmpty()) {
            // 特色情况，结构不同
            searchItem = 10f;
            Elements modules = document.select("#main .module .module-items .module-search-item");
            list = getSearchModules(modules);
        } else {
            searchItem = 16f;
            list = getSearchListSimple(searchList);
        }

        PageResult<MovieItem> searchResult = new PageResult<>();

        Pattern pattern = Pattern.compile("\\$\\('\\.mac_total'\\)\\.[htmlex]+\\('(\\d+)'\\)");
        Matcher matcher = pattern.matcher(document.html());
        if (matcher.find()) {
            searchResult.setTotal(Integer.parseInt(matcher.group(1)));
        }
        searchResult.setResult(list);
        searchResult.setCurrentPage(getPage());
        int page = (int) Math.ceil(searchResult.getTotal() / searchItem);

        searchResult.setLastPage(page);

        return ParserResult.success(searchResult);
    }

    public List<VideoSource> parseSource(Element module, PlayParam playParam, Consumer<VideoSource.Item> consumer) {
        List<String> sources = module.select(".module-tab .module-tab-item span")
                .stream().map(Element::text).collect(Collectors.toList());

        Elements sourceList = module.select(".module-list");

        List<VideoSource> videoSources = new ArrayList<>();
        VideoSource.Item currentSourceItem = null;

        String sourceId = "";
        for (int i = 0; i < sourceList.size(); i++) {
            String sourceName = sources.get(i);
            AtomicInteger index = new AtomicInteger();
            List<VideoSource.Item> items = sourceList.get(i).select("a").stream().map(element -> {
                PlayParam playParam1 = getPlayParamForUrl(element.attr("href"));
                if (playParam1 == null) {
                    return null;
                }
                return new VideoSource.Item(element.text(), index.getAndIncrement(), playParam1);
            }).collect(Collectors.toList());
            if (!items.isEmpty()) {
                sourceId = items.get(0).getParam().getSourceId();
            }

            if (sourceId.equals(playParam.getSourceId())) {
                for (VideoSource.Item item : items) {
                    if (item.getParam().getSelectionId().equals(playParam.getSelectionId())) {
                        currentSourceItem = new VideoSource.Item(item);
                    }
                }
            }

            VideoSource videoSource = new VideoSource(sourceId, sourceName, items);
            videoSources.add(videoSource);
        }
        consumer.accept(currentSourceItem);
        return videoSources;
    }

    @Override
    public ParserResult<MovieDetail> parseDetail(Document document) {
        MovieItem moviesItem = new MovieItem();
        PlayParam playParam;
        Element main = requireNonNull(document.selectFirst(".main"));
        Element content = requireNonNull(main.selectFirst(".content"));
        Elements modules = content.select(".module");

        Element detailModule = modules.get(0);
        Element imgEl = requireNonNull(detailModule.selectFirst(".module-item-pic>img"));
        String cover = imgEl.attr("data-original");
        if (!cover.startsWith("http")) {
            cover = getHost() + cover;
        }
        moviesItem.setCover(cover);
        Element detailMain = requireNonNull(detailModule.selectFirst(".module-info-main"));
        String title = requireNonNull(detailMain.selectFirst("h1")).text();
        moviesItem.setTvName(title);

        Elements tags = detailMain.select(".module-info-tag-link");
        if (!tags.isEmpty()) {
            moviesItem.setYears(tags.get(0).text());
        }

        if (tags.size() > 1) {
            moviesItem.setArea(tags.get(1).text());
        }

        if (tags.size() > 2) {
            moviesItem.setType(tags.get(2).text().replace("/", " "));
        }

        String href = requireNonNull(detailModule.selectFirst(".module-mobile-play a.main-btn"))
                .attr("href");
        playParam = requireNonNull(getPlayParamForUrl(href));
        Elements moduleInfos = detailModule.select(".module-info-item");
        String desc = moduleInfos.get(0).text();
        moviesItem.setDetail(desc);
        String director = requireNonNull(moduleInfos.get(1).selectFirst(".module-info-item-content")).text();
        moviesItem.setDirector(director.replace("/", " "));
        String star = requireNonNull(moduleInfos.get(2).selectFirst(".module-info-item-content")).text();
        moviesItem.setStar(star.replace("/", " "));
        if (moduleInfos.size() > 3) {
            Element premiereEl = moduleInfos.get(3).selectFirst(".module-info-item-content");
            if (premiereEl != null) {
                String premiere = premiereEl.text();
                moviesItem.setPremiere(premiere);
            }
        }
        if (moduleInfos.size() > 4) {
            Element statusEl = moduleInfos.get(4).selectFirst(".module-info-item-content");
            if (statusEl != null) {
                String status = statusEl.text();
                moviesItem.setStatus(status);
            }
        }

        AtomicReference<VideoSource.Item> currentSourceItem = new AtomicReference<>();
        List<VideoSource> videoSources;

        videoSources = parseSource(modules.get(1), playParam, currentSourceItem::set);

        if (currentSourceItem.get() == null) {
            return ParserResult.error("解析错误");
        }
        return ParserResult.success(new MovieDetail(moviesItem, currentSourceItem.get(), videoSources));
    }

    @Override
    public ParserResult<String> parseVideo(Document document) {
        Pattern pattern = Pattern.compile("var player_\\w+=(.+)</script>");
        Matcher matcher = pattern.matcher(document.html());

        if (matcher.find()) {
            String json = matcher.group(1);
            try {
                JSONObject jsonObject = new JSONObject(json);
                String encryptUrl = jsonObject.getString("url");
                // 检查是否进行了URL编码
                /*if (encryptUrl.startsWith("%")) {
                    encryptUrl = URLDecoder.decode(encryptUrl);
                }*/
                Log.i(TAG, "encryptUrl: " + encryptUrl);
                String videoUrl = URLDecoder.decode(encryptUrl);
                if (!encryptUrl.startsWith("http")) {
                    encryptUrl = new String(Base64.decode(encryptUrl, Base64.DEFAULT));
                }
                if (videoUrl.endsWith(".html")) {
                    // videoUrl = parseXm(encryptUrl);
                    videoUrl = parseVideoUrl(encryptUrl);
                }
                Log.i(TAG, "videoUrl: " + videoUrl);
                /*if (videoUrl == null) {
                    videoUrl = parseXMFLV(encryptUrl);
                }*/
                // 去除末尾无效参数 index.m3u8&token=33d5060dcca7 (MYD)
                if (videoUrl == null) {
                    return ParserResult.error("解析地址失败");
                }
                int index = videoUrl.lastIndexOf(".m3u8&");
                if (index != -1) {
                    videoUrl = videoUrl.substring(0, index + 5);
                }
                return ParserResult.success(videoUrl);
            } catch (Exception ignored) {
            }
        }

        return ParserResult.error("解析视频地址失败");
    }

    public static class Video {
        String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public String parseId(String href) {
        Pattern pattern = Pattern.compile("([0-9a-zA-Z]+)\\.html");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseImg(String style) {
        Pattern pattern = Pattern.compile("background: url\\((.+)\\)");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public List<NavItem> parseNav(Document document) {
        Elements navItemEls = document.select(".homepage .navbar .navbar-item a");
        int index = 0;
        return navItemEls.stream().map(element -> {
                    String href = element.attr("href");
                    String id;
                    if ("/".equals(href)) {
                        id = NavItem.HOME;
                    } else {
                        id = parseId(href);
                    }
                    return new NavItem(id, element.text());
                }).filter(navItem -> !navItem.getId().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    protected ParserResult<MainData> parseMain(Document document) {
        // 导航栏
        List<NavItem> navItems = parseNav(document);

        List<BannerItem> bannerList = new ArrayList<>();
        Element content = requireNonNull(document.selectFirst(".content"));
        Elements swiperList = content.select(".container-slide .swiper-big .swiper-slide");

        // Banner
        for (Element swiperItem : swiperList) {
            Element a = requireNonNull(swiperItem.selectFirst("a"));
            String href = a.attr("href");
            String id = parseId(href);
            String style = a.attr("style");
            String cover = parseImg(style);
            String title = requireNonNull(swiperItem.selectFirst(".v-title")).text();
            Element descEl = requireNonNull(swiperItem.selectFirst(".v-ins"));
            String status = descEl.select("p:nth-child(1)").text();
            String desc = descEl.select("p:nth-child(2)").text();
            BannerItem bannerItem = new BannerItem(id, title, cover, status, desc);
            bannerList.add(bannerItem);
        }

        // 卡片布局
        List<MoviesCard<BaseMovieItem>> cards = new ArrayList<>();

        Elements modules = content.getElementsByClass("module");
        for (int i = 0; i < modules.size() - 1; i++) {
            Element module = modules.get(i);
            List<BaseMovieItem> movieItems = new ArrayList<>();
            String title = requireNonNull(module.selectFirst(".module-title"))
                    .removeClass("module-title-en").text();
            MoviesCard<BaseMovieItem> card = new MoviesCard<>(title, movieItems);
            cards.add(card);
            Elements moduleItems = module.select(".module-items .module-item");
            for (Element moduleItem : moduleItems) {
                movieItems.add(parseModuleItem(moduleItem));
            }
        }
        MainData mainData = new MainData(navItems, bannerList, cards, new ArrayList<>());
        return ParserResult.success(mainData);
    }

    public void getLastPage(PageResult<BaseMovieItem> pageResult, Element content) {
        Element pages = content.selectFirst("#page a:nth-last-child(3)");
        if (pages == null) {
            pageResult.setLastPage(1);
            return;
        }
        try {
            pageResult.setLastPage(Integer.parseInt(pages.text()));
        } catch (Exception e) {
            pageResult.setLastPage(1);
        }
    }

    @Override
    public ParserResult<CategoryData> parseClassify(Document document, boolean notParams) {
        PageResult<BaseMovieItem> pageResult = new PageResult<>();
        Element content = requireNonNull(document.selectFirst(".content"));
        Elements modules = content.select(".module .module-main");
        List<FilterData> filterList = null;
        if (notParams) {
            Element filterModule = modules.get(0);
            Elements moduleItems = filterModule.select(".module-class-item");
            filterList = new ArrayList<>();
            for (Element moduleItem : moduleItems) {
                FilterData filterData = new FilterData();
                String title = requireNonNull(moduleItem.selectFirst(".module-item-title")).text();
                filterData.setName(title);
                String filterId = getFilterType(title);
                filterData.setId(filterId);
                Elements filterItem = moduleItem.select(".module-item-box a");
                filterData.setItems(filterItem.stream().map(element -> {
                    FilterData.Item item = new FilterData.Item();
                    item.setGroupId(filterId);
                    // 对类型进行单独处理
                    if (filterId.equals(FilterData.FILTER_CATE)) {
                        Pattern pattern = Pattern.compile(".*?(\\d+).*?");
                        Matcher matcher = pattern.matcher(element.attr("href"));
                        if (matcher.find()) {
                            item.setId(matcher.group(1));
                            item.setName(element.text());
                        } else {
                            item.setId(element.text());
                            item.setName(item.getId());
                        }
                    } else if (filterId.equals(FilterData.FILTER_SORT)) {
                        item.setName(element.text());
                        item.setId(getSortIdByName(item.getName()));
                    } else {
                        item.setId(element.text());
                        item.setName(item.getId());
                    }
                    return item;
                }).collect(Collectors.toList()));
                filterList.add(filterData);
            }
        }

        Elements items;
        if (modules.size() > 1) {
            items = modules.get(1).select(".module-items a");
        } else {
            items = content.select("a.module-item");
        }

        List<BaseMovieItem> movieItems = new ArrayList<>();
        for (Element item : items) {
            movieItems.add(parseModuleItem(item));
        }

        getLastPage(pageResult, content);

        pageResult.setResult(movieItems);

        CategoryData categoryData = new CategoryData(filterList, pageResult);

        return ParserResult.success(categoryData);
    }

    private BaseMovieItem parseModuleItem(Element moduleItem) {
        String name = moduleItem.attr("title");
        String href = moduleItem.attr("href");
        String id = parseId(href);
        String status = requireNonNull(moduleItem.selectFirst(".module-item-note")).text();
        Element picEl = requireNonNull(moduleItem.selectFirst(".module-item-pic img"));
        String cover = picEl.attr("data-original");
        if (!cover.startsWith("http")) {
            cover = getHost() + cover;
        }
        BaseMovieItem movieItem = new BaseMovieItem();
        movieItem.setId(id);
        movieItem.setTvName(name);
        movieItem.setCover(cover);
        movieItem.setStatus(status);
        return movieItem;
    }
}
