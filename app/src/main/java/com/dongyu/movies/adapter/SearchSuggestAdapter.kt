package com.dongyu.movies.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.data.search.SearchSuggestItem
import com.dongyu.movies.databinding.ItemListSearchBinding
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.Checker
import com.dongyu.movies.utils.dp2px
import com.dongyu.movies.utils.ioThread

class SearchSuggestAdapter :
  ListAdapter<SearchSuggestItem, SearchSuggestAdapter.SearchViewHolder>(DIFF_CALL_BACK) {

    init {
      // 验证App签名
      Checker.verifySignature()
    }

  companion object {
    private val DIFF_CALL_BACK = object : DiffUtil.ItemCallback<SearchSuggestItem>() {
      override fun areItemsTheSame(
        oldItem: SearchSuggestItem,
        newItem: SearchSuggestItem
      ): Boolean {
        return if (oldItem is SearchSuggestItem.Item && newItem is SearchSuggestItem.Item) {
          oldItem.suggest.id == newItem.suggest.id
        } else if (oldItem is SearchSuggestItem.Record && newItem is SearchSuggestItem.Record) {
          oldItem.name == newItem.name
        } else {
          oldItem == newItem
        }
      }

      override fun areContentsTheSame(
        oldItem: SearchSuggestItem,
        newItem: SearchSuggestItem
      ): Boolean {
        return oldItem == newItem
      }
    }
  }

  lateinit var onItemClickListener: OnItemClickListener

  class SearchViewHolder(val binding: ItemListSearchBinding) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.tvName.compoundDrawablePadding = 10.dp2px()
    }

    fun bindTo(searchSuggestItem: SearchSuggestItem?) {
      binding.tvName.text = searchSuggestItem?.name
      val icon = if (searchSuggestItem is SearchSuggestItem.Record) R.drawable.baseline_history_24
      else R.drawable.baseline_search_24
      binding.tvName.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
      binding.btnDelete.isVisible = searchSuggestItem is SearchSuggestItem.Record
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return SearchViewHolder(ItemListSearchBinding.inflate(inflater, parent, false)).apply {
      binding.root.setOnClickListener {
        onItemClickListener.onItemClick(it, absoluteAdapterPosition)
      }
      binding.btnDelete.setOnClickListener {
        val item = getItem(absoluteAdapterPosition) as SearchSuggestItem.Record
        item.history.delete()
        val list = currentList.filter { it.name != item.name }
        submitList(list)
      }
    }
  }

  override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
    holder.bindTo(getItem(position))
  }
}