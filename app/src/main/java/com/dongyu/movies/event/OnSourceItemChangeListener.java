package com.dongyu.movies.event;

import com.dongyu.movies.model.movie.VideoSource;

/**
 * 监听源改变时回调接口
 */
public interface OnSourceItemChangeListener {

    /**
     * 当源改变时
     * @param item 指定项
     */
    void onSourceItemChanged(VideoSource.Item item, int position);
}
