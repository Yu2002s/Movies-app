package com.dongyu.movies.parser;

import com.dongyu.movies.model.home.CategoryData;
import com.dongyu.movies.model.home.MainData;
import com.dongyu.movies.model.movie.MovieDetail;
import com.dongyu.movies.model.movie.MovieItem;
import com.dongyu.movies.model.page.PageResult;
import com.dongyu.movies.model.parser.ParserResult;

import org.jsoup.nodes.Document;

public class SimpleParser extends BaseParser<Object> {
    @Override
    public ParserResult<PageResult<MovieItem>> parseSearchList(Document document) {
        return null;
    }

    @Override
    public ParserResult<MovieDetail> parseDetail(Document document) {
        return null;
    }

    @Override
    public ParserResult<String> parseVideo(Document document) {
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
}
