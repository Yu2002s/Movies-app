package com.dongyu.movies;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ParseTest {

    @Test
    public void testParse() {
        /*ClassifyQueryParam param = new ClassifyQueryParam();
        param.setArea("香港");
        BaseParser<Object> parser = new MxThemeParser()
                // .setUrl("https://www.tttv.tv/index.php")
                *//*.setUrl("https://www.tttv.tv/index.php/vod/search.html?wd={name}&page={page}")
                .setPage(1)
                .setName("士兵突击");*//*
                .setUrl("https://www.tttv.tv/index.php/vod/show/area/{area}/id/52.html")
                .setClassifyQueryParam(param);

        ParserResult<CategoryData> classify = parser.getClassify();
        if (classify.isOk()) {
            System.out.println(classify.getData());
        } else {
            System.out.println(classify.getMsg());
        }*/

        /*ParserResult<MainData> main = parser.getMain();
        if (main.isOk()) {
            System.out.println(main.getData());
        } else {
            System.out.println(main.getMsg());
        }*/
        /*ParserResult<PageResult<BaseMovieItem>> result = parser.getSearchList();

        if (result.isOk()) {
            System.out.println(result.getData());
        } else {
            System.out.println(result.getMsg());
        }*/
    }

}
