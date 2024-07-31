package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dongyu.movies.data.home.RankItem
import com.dongyu.movies.databinding.ItemRankBinding
import com.dongyu.movies.event.OnCardItemClickListener
import com.dongyu.movies.event.OnItemClickListener

class RankAdapter(private val rankList: List<RankItem>) :
    RecyclerView.Adapter<RankAdapter.ViewHolder>() {

    var onCardItemClickListener: OnCardItemClickListener? = null

    class ViewHolder(
        val binding: ItemRankBinding,
        onCardItemClickListener: OnCardItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

         val adapter = RankListAdapter()

        init {

            adapter.onItemClickListener = object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onCardItemClickListener?.onCardItemClick(itemView, view, absoluteAdapterPosition, position)
                }
            }
            // binding.rankListRv.isNestedScrollingEnabled = false
            binding.rankListRv.adapter = adapter
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRankBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onCardItemClickListener
        )
    }

    override fun getItemCount(): Int {
        return rankList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rankItem = rankList[position]
         holder.adapter.updateData(rankItem.rankListItems)
        holder.binding.rankTitle.text = rankItem.name
    }


}