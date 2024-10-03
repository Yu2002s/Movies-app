package com.dongyu.movies.model.movie

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dongyu.movies.R
import com.dongyu.movies.databinding.ItemGridMoviesBinding
import com.drake.brv.item.ItemStableId
import com.drake.brv.utils.setup

/**
 * 加载影视宫格列表页
 */
fun RecyclerView.loadListCardMovies(block: (BaseMovieItem) -> Unit) {
    setup {
        addType<BaseMovieItem>(R.layout.item_grid_movies)
        R.id.movie_img.onClick {
            val model = getModel<BaseMovieItem>()
            block(model)
        }
        onBind {
            val model = getModel<BaseMovieItem>()
            getBinding<ItemGridMoviesBinding>().apply {
                movieName.text = model.tvName
                movieStatus.text = model.status
                movieImg.setRadius(30)
                Glide.with(this@loadListCardMovies)
                    .load(model.cover)
                    .override(270, 400)
                    .placeholder(R.drawable.image_loading)
                    .into(movieImg)

            }
        }
    }
}

open class BaseMovieItem(
    /**
     * 唯一id
     */
    var id: String = "",
    /**
     * 影视名称
     */
    var tvName: String = "",
    /**
     * 封面
     */
    var cover: String = "",
    /**
     * 演员信息
     */
    var star: String = "",
    /**
     * 导演
     */
    var director: String = "",
    /**
     * 类别：1：电视剧，2：电影，3：动漫，4综艺
     */
    var cate: String = "",
    /**
     * 状态：评分、集数
     */
    var status: String = ""
): ItemStableId {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseMovieItem

        if (id != other.id) return false
        if (tvName != other.tvName) return false
        if (cover != other.cover) return false
        if (star != other.star) return false
        if (director != other.director) return false
        if (cate != other.cate) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tvName.hashCode()
        result = 31 * result + cover.hashCode()
        result = 31 * result + star.hashCode()
        result = 31 * result + director.hashCode()
        result = 31 * result + cate.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }

    override fun getItemId(): Long {
        return (id + tvName).hashCode().toLong()
    }
}
