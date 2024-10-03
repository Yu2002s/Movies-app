package com.dongyu.movies.model.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 分类查询所需的一些参数
 */
class ClassifyQueryParam {

    /**
     * 当前线路id
     */
    // @SerializedName("movieId")
    // var queryMovieId: Int? = null
    /**
     * 分类id
     */
    var cateId: String = ""
    /**
     * 页码
     */
    var page: Int = 1
    /**
     * 剧集类型（恐怖、戏剧...）
     */
    var type: String? = null
    /**
     * 发行地区
     */
    var area: String? = null
    /**
     * 发行年代
     */
    var year: String? = null
    /**
     * 排序规则
     */
    var sort: String? = null
    /**
     * 根据剧名首字符查询
     */
    var letter: String? = null
    /**
     * 语言
     */
    var language: String? = null
    // @Expose(serialize = false, deserialize = false)
    /**
     * 用于标记当前的影视id
     */
    // var currentMovieId: Int = 1

    constructor()

    constructor(
        language: String? = null,
        letter: String? = null,
        sort: String? = null,
        year: String? = null,
        area: String? = null,
        type: String? = null,
        page: Int = 1,
        cateId: String = "1",
        // queryMovieId: Int? = null
    ) {
        //this.currentMovieId = currentMovieId
        this.language = language
        this.letter = letter
        this.sort = sort
        this.year = year
        this.area = area
        this.type = type
        this.page = page
        this.cateId = cateId
        // this.queryMovieId = queryMovieId
    }

    constructor(params: ClassifyQueryParam) {
        // this.currentMovieId = params.currentMovieId
        this.language = params.language
        this.letter = params.letter
        this.sort = params.sort
        this.year = params.year
        this.area = params.area
        this.type = params.type
        this.page = params.page
        this.cateId = params.cateId
        // this.queryMovieId = params.queryMovieId
    }

    override fun toString(): String {
        return "ClassifyQueryParam(cateId=$cateId, page=$page, type=$type, area=$area, year=$year, sort=$sort, letter=$letter, language=$language)"
    }


}
