package com.dongyu.movies.parser.impl;

import android.annotation.SuppressLint;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 可可影视解析器
 *
 * @apiNote id: 1
 */
public class KeKeParser extends SimpleParser {

    /**
     * 图片静态地址（需要更换）
     */
    private static final String IMG_HOST = "https://61.147.93.252:15002";

    @Override
    public int getParseId() {
        return ParserList.KE_KE.getParseId();
    }

    /**
     * 解析详情页
     *
     * @param document 网页文档对象
     * @return 影视详情
     */
    @Override
    public ParserResult<MovieDetail> parseDetail(Document document) {
        Element mainEl = requireNonNull(document.selectFirst(".t-p .t-p-main .main"));
        // assert mainEl != null : "mainEl";
        Element detailEl = mainEl.selectFirst(".detail-box");
        assert detailEl != null : "detailEl";
        Element imgEl = detailEl.selectFirst(".detail-pic img");
        assert imgEl != null : "imgEl";

        // 影视封面
        String cover = IMG_HOST + imgEl.attr("data-original");

        Element titleEl = detailEl.selectFirst(".detail-title strong:nth-child(2)");
        assert titleEl != null : "titleEl";
        // 影视名称
        String title = titleEl.text();

        // 影视标签
        List<String> tags = detailEl.select(".detail-tags a")
                .stream().map(Element::text).collect(Collectors.toList());

        // 年份
        String year = (tags.get(0));
        // 地区
        String area = tags.get(1);
        // 分类
        String cate = tags.get(2);

        Element playEl = detailEl.selectFirst(".detail-controls a");
        assert playEl != null : "playEl";
        // 播放地址
        String playUrl = playEl.attr("href");
        // 播放参数
        PlayParam playParam = getPlayParamForUrl(playUrl);

        Element detailMainEl = detailEl.selectFirst(".detail-box-main");
        assert detailMainEl != null : "detailMainEl";
        Element descEl = detailMainEl.selectFirst(".detail-desc");
        assert descEl != null : "descEl";
        String desc = descEl.text();

        Elements infoRowEls = detailMainEl.select(".detail-info-row .detail-info-row-main");

        String director = infoRowEls.get(0).select("a").stream().map(Element::text)
                .collect(Collectors.joining(" "));

        String star = infoRowEls.get(1).select("a").stream().map(Element::text)
                .collect(Collectors.joining(" "));

        // 首映日期
        String premiere = infoRowEls.get(2).text();
        MovieItem movieItem = new MovieItem();
        assert playParam != null : "playParam";
        movieItem.setId(playParam.getDetailId());
        movieItem.setTvName(title);
        movieItem.setCate(cate);
        movieItem.setCover(cover);
        movieItem.setStar(star);
        movieItem.setDirector(director);
        movieItem.setDetail(desc);
        movieItem.setPremiere(premiere);
        movieItem.setYears(year);
        movieItem.setArea(area);

        List<VideoSource> sourceList = new ArrayList<>();
        Element episodeBox = mainEl.selectFirst(".episode-box");

        VideoSource.Item sourceItem = null;

        if (episodeBox != null) {
            // 视频播放源
            List<String> sources = episodeBox.select(".source-box .source-item span:nth-child(2)")
                    .stream().map(Element::text).collect(Collectors.toList());

            Elements episodeListEl = episodeBox.select(".episode-box-main .episode-list");

            for (int i = 0; i < episodeListEl.size(); i++) {
                String videoSourceName = sources.get(i);
                // 转换成每一个播放项
                AtomicInteger index = new AtomicInteger();
                List<VideoSource.Item> items = episodeListEl.get(i).select("a").stream().map(element -> {
                    PlayParam param = getPlayParamForUrl(element.attr("href"));
                    if (param == null) {
                        return null;
                    }
                    return new VideoSource.Item(element.text(), index.getAndIncrement(), param);
                }).collect(Collectors.toList());
                String routeId = "";
                if (!items.isEmpty()) {
                    routeId = items.get(0).getParam().getSourceId();
                }
                if (routeId.equals(playParam.getSourceId())) {
                    for (VideoSource.Item item : items) {
                        if (item.getParam().getSelectionId().equals(playParam.getSelectionId())) {
                            sourceItem = new VideoSource.Item(item);
                        }
                    }
                }
                VideoSource source = new VideoSource(routeId, videoSourceName, items);
                sourceList.add(source);
            }
        }

        if (sourceItem == null) {
            return ParserResult.error("解析源错误");
        }

        return ParserResult.success(new MovieDetail(movieItem, sourceItem, sourceList));
    }

    /**
     * 通过播放链接解析出播放所需的一些参数
     *
     * @param href 播放地址
     * @return 播放属性
     */
    @Nullable
    @Override
    public PlayParam getPlayParamForUrl(String href) {
        // /play/241297-32-1034139.html
        Pattern pattern = Pattern.compile("/play/(\\d+)-(\\d+)-(\\d+).html");
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

    @Override
    public ParserResult<PageResult<MovieItem>> parseSearchList(Document document) {
        PageResult<MovieItem> pageResult = new PageResult<>();

        Element main = document.selectFirst(".t-p .t-p-main .main");
        if (main == null) {
            return ParserResult.error("解析失败");
        }

        int total = Integer.parseInt(main.select(".highlight-text").get(1).text());

        // 每页固定18条数据
        int lastPage = (int) Math.ceil(total / 18f);
        pageResult.setLastPage(lastPage);
        pageResult.setTotal(total);

        List<MovieItem> items = new ArrayList<>();
        pageResult.setResult(items);
        Elements searchItems = main.select(".search-result-list .search-result-item");
        for (Element searchItem : searchItems) {
            MovieItem item = new MovieItem();
            String href = searchItem.attr("href");
            String cover = IMG_HOST + searchItem.selectFirst(".search-result-item-pic img:nth-child(2)").attr("data-original");
            Element searchMain = searchItem.selectFirst(".search-result-item-main");
            String title = searchMain.selectFirst(".title:nth-child(2)").text();
            item.setCover(cover);
            item.setTvName(title);
            String[] tags = searchMain.selectFirst(".tags").text().split("/");
            if (tags.length > 0) {
                item.setYears(tags[0]);
            }
            if (tags.length > 1) {
                item.setArea(tags[1]);
            }
            if (tags.length > 2) {
                item.setType(tags[2]);
            }
            item.setCate(searchItem.selectFirst(".search-result-item-header").text());
            /*item.setCate(tags.get(0).text());
            item.setStatus(tags.get(1).text());*/
            item.setStar(searchMain.selectFirst(".actors").text());
            String id = parseId(href);
            item.setId(id);
            items.add(item);
        }

        return ParserResult.success(pageResult);
    }

    @Override
    public ParserResult<String> parseVideo(Document document) {
        Elements scripts = document.select("script");

        Element script = requireNonNull(scripts.get(scripts.size() - 2));
        Pattern pattern = Pattern.compile("window.whatTMDwhatTMDPPPP = '(.+)';");
        Matcher matcher = pattern.matcher(script.html());
        if (matcher.find()) {
            String encryptUrl = matcher.group(1);
            String decryptVideoUrl = decryptVideoUrl(encryptUrl);
            return ParserResult.success(decryptVideoUrl);
        }
        return ParserResult.error("解析播放地址失败");
    }

    @Override
    protected ParserResult<MainData> parseMain(Document document) {
        Element tpMain = requireNonNull(document.selectFirst(".t-p .t-p-main"));
        Elements nav = tpMain.select(".nav-box .nav-item");
        List<NavItem> navItems = nav.stream().map(element -> {
            String href = element.attr("href");
            String id;
            if ("/".equals(href)) {
                id = NavItem.HOME;
            } else {
                id = parseId(href);
            }
            if (id == null) {
                id = "";
            }
            String name = element.text();
            NavItem navItem = new NavItem(id, name);
            if ("短剧".equals(name)) {
                navItem.setType(NavItem.TYPE_SHORT);
            }
            return navItem;
        }).filter(item -> !item.getId().isEmpty()).collect(Collectors.toList());
        Element main = tpMain.selectFirst(" .main");
        if (main == null) {
            return ParserResult.error("解析失败: main");
        }

        List<BannerItem> bannerItems = new ArrayList<>();
        Elements carouselList = main.select(".carousel-box .carousel-item");
        for (Element carousel : carouselList) {
            Element img = carousel.selectFirst("img");
            assert img != null : "解析失败: img";
            String cover = IMG_HOST + img.attr("data-original");
            String href = carousel.attr("href");
            String id = parseId(href);
            assert id != null : "解析失败: id";
            Element titleEl = carousel.selectFirst(".carousel-item-title");
            assert titleEl != null : "解析失败：titleEl";
            String title = titleEl.text();
            Element tagsEl = carousel.selectFirst(".carousel-item-tags");
            assert tagsEl != null : "解析失败: tagsEl";
            String status = tagsEl.text().replace(" ", "");
            Element itemDesc = carousel.selectFirst(".carousel-item-desc");
            assert itemDesc != null : "解析失败: itemDesc";
            String desc = itemDesc.text();
            BannerItem bannerItem = new BannerItem(id, title, cover, status, desc);
            bannerItems.add(bannerItem);
        }

        Elements sectionList = main.select(".section-box");

        List<MoviesCard<BaseMovieItem>> moviesCards = new ArrayList<>();

        for (Element section : sectionList) {
            Element headerTitle = section.selectFirst(".section-header-title");
            assert headerTitle != null : "解析失败: headerTitle";
            String title = headerTitle.text();
            if (title.equals("专题列表")) {
                continue;
            }
            List<BaseMovieItem> items = new ArrayList<>();
            MoviesCard<BaseMovieItem> moviesCard = new MoviesCard<>(title, items);
            moviesCards.add(moviesCard);
            Elements tvList = section.select(".section-main .module-item a");
            for (Element tv : tvList) {
                BaseMovieItem item = new BaseMovieItem();
                String id = parseId(tv.attr("href"));
                assert id != null;
                Elements imgEls = tv.select("img");
                String cover = IMG_HOST + imgEls.get(1).attr("data-original");
                Element nameEl = tv.selectFirst(".v-item-footer .v-item-title:nth-child(2)");
                String name = requireNonNull(nameEl).text();

                Element itemTop = tv.selectFirst(".v-item-top");
                assert itemTop != null;
                String status = itemTop.text();
                if (status.isEmpty()) {
                    Elements itemBottoms = tv.select(".v-item-bottom span");
                    status = itemBottoms.get(1).text();
                }
                item.setStatus(status);
                item.setCover(cover);
                item.setTvName(name);
                item.setId(id);
                items.add(item);
            }
        }

        MainData mainData = new MainData(navItems, bannerItems, moviesCards, Collections.emptyList());

        return ParserResult.success(mainData);
    }

    @Override
    public ParserResult<CategoryData> parseClassify(Document document, boolean notParams) {
        Element main = document.selectFirst(".t-p .t-p-main .main");
        if (main == null) {
            return ParserResult.error("失败");
        }
        List<FilterData> filterData = null;

        if (notParams) {
            filterData = new ArrayList<>();
            Elements filterRows = main.select(".filter-box .filter-row");
            // 类型id集合
            String[] ids = new String[]{FilterData.FILTER_TYPE, FilterData.FILTER_AREA, FilterData.FILTER_LANGUAGE,
                    FilterData.FILTER_YEAR, FilterData.FILTER_SORT};
            for (int i = 0; i < filterRows.size(); i++) {
                Element filterRow = filterRows.get(i);
                FilterData filterDataItem = new FilterData();
                String title = filterRow.selectFirst(".filter-row-side").text();
                filterDataItem.setName(title);
                filterDataItem.setId(ids[i]);
                Elements filterItems = filterRow.select("a.filter-item");
                List<FilterData.Item> items = getItems(filterItems, i, filterRows);
                filterDataItem.setItems(items);
                filterData.add(filterDataItem);
            }
        }

        int page = getClassifyQueryParam().getPage();
        PageResult<BaseMovieItem> pageResult = new PageResult<>();
        List<BaseMovieItem> items = new ArrayList<>();
        pageResult.setResult(items);
        Elements moduleItems = main.select(".section-box .module-item a");
        for (Element moduleItem : moduleItems) {
            BaseMovieItem item = new BaseMovieItem();
            String id = parseId(moduleItem.attr("href"));
            item.setId(id);
            Element img = moduleItem.selectFirst(".v-item-cover img:nth-child(2)");
            String cover = IMG_HOST + img.attr("data-original");
            item.setCover(cover);
            Element nameEl = moduleItem.selectFirst(".v-item-footer .v-item-title:nth-child(2)");
            String name = requireNonNull(nameEl).text();
            // item.setTvName(img.attr("title"));
            item.setTvName(name);
            item.setStatus(moduleItem.selectFirst(".v-item-bottom span:nth-child(2)").text());
            items.add(item);

        }
        if (items.size() < 18) {
            pageResult.setLastPage(page);
        } else {
            pageResult.setLastPage(page + 1);
        }

        return ParserResult.success(new CategoryData(filterData, pageResult));
    }

    private static @NotNull List<FilterData.Item> getItems(Elements filterItems, int i, Elements filterRows) {
        List<FilterData.Item> items = new ArrayList<>();
        for (int j = 0; j < filterItems.size(); j++) {
            Element filterItem = filterItems.get(j);
            FilterData.Item item = new FilterData.Item();
            item.setValue(filterItem.text());
            item.setName(item.getValue());
            if (i == 1) {
                if (j > 0 && j < 4) {
                    item.setId("中国" + item.getValue());
                }
            } else if (i == filterRows.size() - 1) {
                item.setId(String.valueOf(j + 1));
            }
            items.add(item);
        }
        return items;
    }

    @Nullable
    private String parseId(String href) {
        Pattern pattern = Pattern.compile("(\\d+).html");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static final String DECRYPT_KEY = "VNF9aVQF!G*0ux@2hAigUeH3";

    private static final String DECRYPT_MODE = "AES/ECB/PKCS7Padding";

    /**
     * 解密视频地址
     *
     * @param encryptStr 加密字符
     * @return 解密的地址
     */
    @Nullable
    private String decryptVideoUrl(String encryptStr) {
        try {
            // 将Base64编码的字符串转换成字节数组
            byte[] cipherText;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cipherText = Base64.getDecoder().decode(encryptStr);
            } else {
                cipherText = android.util.Base64.decode(encryptStr, android.util.Base64.DEFAULT);
            }

            // 创建一个密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(DECRYPT_KEY.getBytes(), "AES");

            // 创建一个Cipher实例，指定解密模式为AES/ECB/PKCS5Padding（PKCS5Padding兼容PKCS7Padding）
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(DECRYPT_MODE);

            // 初始化Cipher实例进行解密
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            // 执行解密
            byte[] decryptedBytes = cipher.doFinal(cipherText);

            return new String(decryptedBytes);

        } catch (Exception e) {
            Log.e(TAG, "地址解密失败: " + e);
            return null;
        }
    }
}
