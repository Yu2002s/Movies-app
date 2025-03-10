package com.dongyu.movies.parser;

import com.dongyu.movies.parser.impl.CCParser;
import com.dongyu.movies.parser.impl.DDParser;
import com.dongyu.movies.parser.impl.DouBanParser;
import com.dongyu.movies.parser.impl.KeKeParser;
import com.dongyu.movies.parser.impl.MJParser;
import com.dongyu.movies.parser.impl.MxThemeParser;
import com.dongyu.movies.parser.impl.MxThemeParser2;
import com.dongyu.movies.parser.impl.XZParser;
import com.dongyu.movies.parser.impl.YJParser;

public enum ParserList {

    KE_KE(1, new KeKeParser()),
    MX_THEME(2, new MxThemeParser()),
    MX_THEME2(3, new MxThemeParser2()),
    MJ(4, new MJParser()),
    CC(5, new CCParser()),
    YJ(6, new YJParser()),
    DD(7, new DDParser()),
    DB(8, new DouBanParser()),
    XZ(9, new XZParser());

    /**
     * 唯一的解析id
     */
    private final int id;

    /**
     * 解析器实例
     */
    private final BaseParser<Object> parser;

    ParserList(int id, BaseParser<Object> parser) {
        this.id = id;
        this.parser = parser;
    }

    /**
     * 获取当前解析器id
     *
     * @return 解析id
     */
    public int getParseId() {
        return id;
    }

    /**
     * 通过id获取指定解析器
     *
     * @param id 解析id
     * @return 解析器实例
     */
    public static BaseParser<Object> getParser(int id) {
        for (ParserList value : values()) {
            if (id == value.id) {
                return value.parser;
            }
        }
        return MX_THEME.parser;
    }
}
