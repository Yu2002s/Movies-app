package com.dongyu.movies.network

import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.base.requestCallFlow
import com.dongyu.movies.base.requestCallResult
import com.dongyu.movies.data.search.History
import com.dongyu.movies.data.search.SearchParam
import com.dongyu.movies.data.search.SearchSuggestItem
import kotlinx.coroutines.flow.flowOf
import org.litepal.LitePal
import org.litepal.extension.find
import retrofit2.await

object SearchRepository {

    private val searchService = BaseRepository.searchService()

    suspend fun getSearchSuggest(name: String) = flowOf(runCatching {
        if (name.isBlank()) {
            LitePal.limit(20).order("updatedAt desc").find<History>()
                .map { SearchSuggestItem.Record(it) }
        } else {
            val response = searchService.getSearchSuggest(name).await()
            if (response.code == 200) {
                response.data.map {
                    SearchSuggestItem.Item(it)
                }
            } else {
                throw Throwable(response.msg)
            }
        }
    })

    suspend fun getSearchTVList(movieId: Int, name: String, page: Int) =
        requestCallResult {
            searchService.getSearchTVList(movieId, name, page)
        }

}