package com.dongyu.movies.parser;

import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieItem;
import com.dongyu.movies.model.movie.MovieVideo;
import com.dongyu.movies.model.page.PageResult;
import com.dongyu.movies.model.parser.ParserResult;
import com.dongyu.movies.model.search.SearchData;

/**
 * 解析规范接口
 * @param <T> 普通解析需要的类型支持
 */
public interface HtmlParseable<T> {

    /**
     * 普通解析
     * @return 解析结果
     */
    ParserResult<T> parse();

    /**
     * 解析搜索页数据
     *
     * @return 返回搜索到的多页影视数据
     */
    ParserResult<SearchData> getSearchList();

    /**
     * 解析详情页数据
     * @return 返回影视详情对象
     */
    ParserResult<MovieDetail> getDetail();

    /**
     * 解析视频播放地址
     *
     * @return 返回地址信息
     */
    ParserResult<MovieVideo> getVideo();

    /**
     * 解析主页数据
     * @return 主页对象
     */
    ParserResult<MainData> getMain();

    /**
     * 解析影视分类数据
     * @return 各分类的影视数据
     */
    ParserResult<CategoryData> getClassify();
}
