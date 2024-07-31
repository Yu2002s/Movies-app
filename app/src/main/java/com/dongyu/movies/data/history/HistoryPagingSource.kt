package com.dongyu.movies.data.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dongyu.movies.utils.getRelTime
import org.litepal.LitePal
import org.litepal.extension.count
import org.litepal.extension.find
import kotlin.math.ceil

class HistoryPagingSource: PagingSource<Int, PlayHistory>() {

    override fun getRefreshKey(state: PagingState<Int, PlayHistory>) = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlayHistory> {

        val page = params.key ?: 1

        val count = LitePal.count<PlayHistory>()
        val endPage = ceil(count / 10f).toInt()

        val offset = (page - 1) * 10

        val histories = LitePal.offset(offset).limit(10)
            .order("updateAt desc").find<PlayHistory>()

        histories.forEach {
            it.timeStr = it.updateAt.getRelTime()
        }

        return LoadResult.Page(histories, null, if (page < endPage) page + 1 else null)
    }
}