package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dongyu.movies.data.article.Article
import com.dongyu.movies.data.article.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val ITEMS_PER_PAGE = 20

/*class ArticleViewModelFactory(private val repository: ArticleRepository): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            return ArticleViewModel(repository) as T
        }
        throw IllegalArgumentException()
    }
}*/

class ArticleViewModel : ViewModel() {

    val items: Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = ITEMS_PER_PAGE,
            enablePlaceholders = true,
            prefetchDistance = 3,
            initialLoadSize = 10
        ),
        pagingSourceFactory = { ArticleRepository.articlePagingSource() }
    )
        .flow
        .cachedIn(viewModelScope)
}