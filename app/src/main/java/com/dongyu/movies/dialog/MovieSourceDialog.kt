package com.dongyu.movies.dialog

import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dongyu.movies.model.movie.MovieResponse
import com.dongyu.movies.network.Repository
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.MainActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MovieSourceDialog(context: AppCompatActivity, private val callback: (() -> Unit)? = null) :
    MaterialAlertDialogBuilder(context) {

    private val mainViewModel by context.viewModels<MainActivityViewModel>()

    private val progressDialog = MaterialProgressDialog(context)

    init {
        progressDialog.setMessage("正在获取线路中...请稍后")
        val dialog = progressDialog.show()
        setTitle("选择主页展示线路")
        context.lifecycleScope.launch {
            mainViewModel.movieListState().collect {
                dialog.dismiss()
                showMoviesList(it)
            }
        }
    }

    private fun showMoviesList(result: Result<List<MovieResponse.Movie>>) {
        result.onSuccess { list ->
            val currentMovieId = Repository.currentMovieId
            val index = list.indexOfFirst {
                it.id == currentMovieId
            }
            setSingleChoiceItems(list.map {
                if (it.desc.isNullOrEmpty()) {
                    it.name
                } else {
                    it.name + "(${it.desc})"
                }
            }.toTypedArray(), index) { dialog, position ->
                Repository.currentMovie = list[position]
                callback?.invoke()
                dialog.dismiss()
            }
            setNegativeButton("刷新") { _, _ ->
                callback?.invoke()
            }
            setPositiveButton("关闭", null)
            show()
        }.onFailure {
            Log.e("jdy", it.toString())
            it.message.showToast()
        }
    }
}