package com.dongyu.movies.parser.impl;

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
import com.dongyu.movies.parser.BaseParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MxThemeParser2 extends MxThemeParser {
    @Override
    public ParserResult<MovieDetail> parseDetail(Document document) {
        PlayParam playParam;
        MovieItem movieItem = new MovieItem();
        Element content = requireNonNull(document.selectFirst("#main .content:nth-child(1)"));
        String cover = requireNonNull(content.selectFirst(".module-item-cover .module-item-pic img"))
                .attr("data-src");
        if (!cover.startsWith("http")) {
            cover = getHost() + cover;
        }
        Element videoInfo = requireNonNull(content.selectFirst(".video-info"));
        Element headerEl = requireNonNull(videoInfo.selectFirst(".video-info-header"));
        String title = requireNonNull(headerEl.selectFirst("h1")).text();

        Elements tags = videoInfo.select(".video-info-aux .tag-link");
        String cate = tags.get(0).text();
        String type = tags.get(1).text().replace("/", " ");
        String year = tags.get(2).text();
        String area = tags.get(3).text();
        String href = requireNonNull(videoInfo.selectFirst(".video-info-play")).attr("href");
        playParam = getPlayParamForUrl(href);
        assert playParam != null;
        Elements infoItems = videoInfo.select(".video-info-main .video-info-item");
        if (!infoItems.isEmpty()) {
            String director = infoItems.get(0).text().replace("/", " ");
            movieItem.setDirector(director);
        }
        if (infoItems.size() > 1) {
            String star = infoItems.get(1).text().replace("/", " ");
            movieItem.setStar(star);
        }
        if (infoItems.size() > 3) {
            String status = infoItems.get(3).text();
            movieItem.setStatus(status);
        }
        if (infoItems.size() > 5) {
            String detail = infoItems.get(5).text();
            movieItem.setDetail(detail);
        }
        movieItem.setTvName(title);
        movieItem.setCover(cover);
        movieItem.setCate(cate);
        movieItem.setType(type);
        movieItem.setYears(year);
        movieItem.setArea(area);

        /*Element playInfo = requireNonNull(content.selectFirst(".player-info"));

        Element infoBox = requireNonNull(playInfo.selectFirst(".video-info-box"));
        Elements titleEl = infoBox.select(".page-title");
        String title = titleEl.get(0).text();
        String status = titleEl.get(1).text();

        Elements tags = infoBox.select(".video-info-aux .tag-link");
        String cate = tags.get(0).text();
        String type = tags.get(1).text().replace("/", " ");
        String year = tags.get(2).text();
        String area = tags.get(3).text();

        String detail = requireNonNull(infoBox.selectFirst(".video-info-main .video-info-content")).text();
        String href = requireNonNull(playInfo.selectFirst(".video-player-handle a")).attr("href");
        playParam = getPlayParamForUrl(href);
        movieItem.setTvName(title);
        movieItem.setCate(cate);
        movieItem.setType(type);
        movieItem.setYears(year);
        movieItem.setArea(area);
        movieItem.setStatus(status);
        movieItem.setDetail(detail);*/

        Element sourceModule = requireNonNull(content.selectFirst(".module"));

        List<String> sources = sourceModule.select(".module-tab .module-tab-items .module-tab-item span").stream()
                .map(Element::text).collect(Collectors.toList());

        Elements sourceList = sourceModule.select(".module-list .scroll-content");

        List<VideoSource> videoSources = new ArrayList<>();

        VideoSource.Item currentSourceItem = null;
        String sourceId = "";
        for (int i = 0; i < sources.size(); i++) {
            String sourceName = sources.get(i);
            AtomicInteger index = new AtomicInteger();
            List<VideoSource.Item> items = sourceList.get(i).select("a").stream().map(new Function<Element, VideoSource.Item>() {
                @Override
                public VideoSource.Item apply(Element element) {
                    PlayParam playParam1 = getPlayParamForUrl(element.attr("href"));
                    if (playParam1 == null) {
                        return null;
                    }
                    return new VideoSource.Item(element.text(), index.getAndIncrement(), playParam1);
                }
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

        if (currentSourceItem == null) {
            return ParserResult.error("解析错误");
        }

        return ParserResult.success(new MovieDetail(movieItem, currentSourceItem, videoSources));
    }

    @Override
    public List<NavItem> parseNav(Document document) {
        Elements navItemEls = document.select(".homepage .nav .nav-menu-item a");
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

    private List<BaseMovieItem> parseModuleItems(Element module) {
        Elements items = module.select(".module-items .module-item");
        return items.stream().map(element -> {
            Element picEl = requireNonNull(element.selectFirst(".module-item-pic"));
            Element a = requireNonNull(picEl.selectFirst("a"));
            String tvName = a.attr("title");
            String id = parseId(a.attr("href"));
            String cover = requireNonNull(picEl.selectFirst("img")).attr("data-src");
            if (!cover.startsWith("http")) {
                cover = getHost() + cover;
            }
            String status = requireNonNull(element.selectFirst(".module-item-text")).text();
            BaseMovieItem movieItem = new BaseMovieItem();
            movieItem.setTvName(tvName);
            movieItem.setCover(cover);
            movieItem.setId(id);
            movieItem.setStatus(status);
            return movieItem;
        }).collect(Collectors.toList());
    }

    @Override
    protected ParserResult<MainData> parseMain(Document document) {
        Elements modules = document.select(".content .module");
        List<MoviesCard<BaseMovieItem>> cards = new ArrayList<>();
        for (Element module : modules) {
            String title = requireNonNull(module.selectFirst(".module-title")).ownText();
            List<BaseMovieItem> movieItems = parseModuleItems(module);
            cards.add(new MoviesCard<>(title, movieItems));
        }

        return ParserResult.success(new MainData(parseNav(document),
                Collections.emptyList(), cards, Collections.emptyList()));
    }

    @Override
    public ParserResult<CategoryData> parseClassify(Document document, boolean notParams) {
        List<FilterData> filterDataList = null;
        if (notParams) {
            /*Elements types = document.select(".content .block-box-item .block-box-content");
            FilterData filterData = new FilterData(FilterData.FILTER_TYPE, "类型", types
                    .stream().map(element -> {
                        FilterData.Item item = new FilterData.Item();
                        item.setId(parseId(element.attr("href")));
                        item.setName(element.attr("title"));
                        item.setGroupId(FilterData.FILTER_TYPE);
                        return item;
                    }).collect(Collectors.toList()));
            filterDataList = new ArrayList<>();
            filterDataList.add(filterData);*/

            Elements filters = document.select(".content .library-box");
            /*for (int i = 0; i < 2; i++) {
                filters.get(i).remove();
            }
            filters.get(filters.size() - 1).remove();*/
            String[] ids = new String[]{FilterData.FILTER_TYPE, FilterData.FILTER_AREA, FilterData.FILTER_LANGUAGE,
                    FilterData.FILTER_YEAR, FilterData.FILTER_LETTER, FilterData.FILTER_SORT};
            String[] titles = new String[]{"类型", "地区", "语言", "年份", "字母", "排序"};
            filterDataList = new ArrayList<>();
            for (int i = 2; i < filters.size() - 1; i++) {
                Element filter = filters.get(i);
                FilterData filterData = new FilterData();
                filterData.setId(ids[i - 2]);
                filterData.setName(titles[i - 2]);
                List<FilterData.Item> items = filter.select(".library-item").stream().map(new Function<Element, FilterData.Item>() {
                    @Override
                    public FilterData.Item apply(Element element) {
                        FilterData.Item item = new FilterData.Item();
                        if (filterData.getId().equals(FilterData.FILTER_TYPE)) {
                            item.setGroupId("cateId");
                        } else {
                            item.setGroupId(filterData.getId());
                        }
                        item.setName(element.text());
                        item.setId(item.getName());
                        return item;
                    }
                }).collect(Collectors.toList());
                filterData.setItems(items);
                filterDataList.add(filterData);
            }
        }

        Elements modules = document.select(".content .module");
        PageResult<BaseMovieItem> pageResult = new PageResult<>();
        List<BaseMovieItem> movieItems = new ArrayList<>();
        for (Element module : modules) {
            movieItems.addAll(parseModuleItems(module));
        }

        getLastPage(pageResult, requireNonNull(document.selectFirst(".content .module .module-footer")));

        pageResult.setResult(movieItems);

        return ParserResult.success(new CategoryData(filterDataList, pageResult));
    }
}
