package com.dongyu.movies.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dongyu.movies.adapter.ArticleAdapter
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivityArticleBinding
import com.dongyu.movies.viewmodel.ArticleViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArticleActivity : BaseActivity() {

    private val binding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val toolBar = binding.header.toolBar
        setSupportActionBar(toolBar)
        supportActionBar?.let {
            it.title = "Test"
            it.setDisplayHomeAsUpEnabled(true)
        }

        val viewModel by viewModels<ArticleViewModel>()

        val articleAdapter = ArticleAdapter()

        binding.articleRecyclerView.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(this@ArticleActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@ArticleActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collect {
                    binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collectLatest {
                    articleAdapter.submitData(it)
                }
            }
        }
    }

    override fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
        return true
    }
}