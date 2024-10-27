package com.dongyu.movies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dongyu.movies.databinding.ItemListBannerBinding
import com.dongyu.movies.model.home.BannerItem
import com.youth.banner.adapter.BannerAdapter

class HomeBannerAdapter(data: List<BannerItem>? = null) :
    BannerAdapter<BannerItem, HomeBannerAdapter.ViewHolder>(data) {

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemListBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindView(
        holder: ViewHolder,
        data: BannerItem,
        position: Int,
        size: Int
    ) {
        val binding = holder.binding
        binding.bannerTitle.text = data.name
        binding.bannerContent.isVisible = !data.desc.isNullOrBlank()
        binding.bannerContent.text = data.desc
        binding.bannerImg.setRadius(30)
        Glide.with(holder.itemView)
            .load(data.cover)
            .into(binding.bannerImg)
    }

    class ViewHolder(val binding: ItemListBannerBinding): RecyclerView.ViewHolder(binding.root)
}