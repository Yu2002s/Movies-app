package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.data.article.Article
import com.dongyu.movies.databinding.ItemListArticleBinding

class ArticleViewHolder(
  viewGroup: ViewGroup,
  binding: ItemListArticleBinding =
    ItemListArticleBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
) :
  RecyclerView.ViewHolder(binding.root) {

  private val nameText = binding.articleName
  private val timeText = binding.articleTime

  fun bindTo(article: Article?) {
    nameText.text = article?.title
    timeText.text = article?.createdTime?.time.toString()
  }
}

class ArticleAdapter : PagingDataAdapter<Article, ArticleViewHolder>(ARTICLE_DIFF_CALLBACK) {

  companion object {
    private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
      override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
      }
    }
  }

  override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
    holder.bindTo(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
    return ArticleViewHolder(parent)
  }
}