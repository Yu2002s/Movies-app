package com.dongyu.movies.data.article

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.max

class ArticlePagingSource : PagingSource<Int, Article>() {

    private val firstArticleCreatedTime = Date()

    companion object {
        private const val STARTING_KEY = 0;
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null
        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val start = params.key ?: STARTING_KEY
        val range = start.until(start + params.loadSize)

        if (start != STARTING_KEY) delay(3000)

        return LoadResult.Page(
            data = range.map { number ->
                Article(
                    id = number,
                    title = "Article $number",
                    createdTime = firstArticleCreatedTime
                )
            },
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
             if (range.last > 20) null else range.last + 1,
            itemsAfter = 2,
            itemsBefore = 2
        )
    }

    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)

}