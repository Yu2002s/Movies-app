package com.dongyu.movies.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.R
import com.dongyu.movies.data.home.FilterData
import com.dongyu.movies.databinding.ItemFilterBinding
import com.dongyu.movies.databinding.ItemListFilterBinding
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.event.OnItemClickListener

class FilterAdapter(private val filterList: List<FilterData>) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    var onCardItemClickListener: OnCardItemClickListener? = null

    class ViewHolder(val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        val adapter = FilterItemAdapter()

        init {
            binding.filterList.adapter = adapter
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            adapter.onItemClickListener = object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onCardItemClickListener?.onCardItemClick(
                        itemView,
                        view,
                        absoluteAdapterPosition,
                        position
                    )
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterData = filterList[position]
        holder.binding.filterName.text = filterData.name
        holder.adapter.submitList(filterData.items)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        val innerPosition = payloads[0] as Int
        holder.adapter.notifyItemChanged(innerPosition)
    }

    class FilterItemAdapter : RecyclerView.Adapter<FilterItemAdapter.ViewHolder>() {

        private val items = mutableListOf<FilterData.Item>()

        @SuppressLint("NotifyDataSetChanged")
        fun submitList(list: List<FilterData.Item>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        var onItemClickListener: OnItemClickListener? = null

        class ViewHolder(textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemListFilterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ).root
            ).apply {
                itemView.setOnClickListener {
                    onItemClickListener?.onItemClick(it, absoluteAdapterPosition)
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            (holder.itemView as TextView).apply {
                text = item.value
                setTypeface(null, if (item.isSelect) Typeface.BOLD else Typeface.NORMAL)
            }
            holder.itemView.isSelected = item.isSelect
        }
    }
}