package com.dongyu.movies.dialog

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dongyu.movies.config.SPConfig
import com.dongyu.movies.data.movie.MovieResponse
import com.dongyu.movies.utils.SpUtils.get
import com.dongyu.movies.utils.SpUtils.put
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.MainActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RouteDialog(context: AppCompatActivity, private val callback: (() -> Unit)? = null) :
    MaterialAlertDialogBuilder(context) {

    private val mainViewModel by context.viewModels<MainActivityViewModel>()

    init {
        context.lifecycleScope.launch {
            mainViewModel.movieListState().collect {
                showMoviesList(it)
            }
        }
    }

    private fun showMoviesList(result: Result<List<MovieResponse.Movie>>) {
        result.onSuccess { list ->
            val currentMovieId = SPConfig.CURRENT_ROUTE_ID.get<Int?>(-1)
            val index = list.indexOfFirst {
                it.id == currentMovieId
            }
            setTitle("线路列表")
            setSingleChoiceItems(list.map { it.name }.toTypedArray(), index) { dialog, position ->
                SPConfig.CURRENT_ROUTE_ID put list[position].id
                callback?.invoke()
                dialog.dismiss()
            }
            setNegativeButton("刷新") { _, _ ->
                callback?.invoke()
            }
            setNeutralButton("关闭", null)
            show()
        }.onFailure {
            it.message.showToast()
        }
    }
}