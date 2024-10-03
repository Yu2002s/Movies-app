package com.dongyu.movies.adapter

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cat.sdk.ad.ADBannerAd
import com.cat.sdk.ad.ADBannerAd.ADBannerAdListener
import com.cat.sdk.ad.ADMParams
import com.dongyu.movies.R
import com.dongyu.movies.config.ADConfig
import com.dongyu.movies.databinding.ItemListBannerBinding
import com.dongyu.movies.model.home.BannerItem
import com.youth.banner.adapter.BannerAdapter

class HomeBannerAdapter(data: List<BannerItem>? = null) :
    BannerAdapter<BannerItem, HomeBannerAdapter.BannerViewHolder>(data) {

    companion object {

        private const val NORMAL = 0

        private const val AD = 1

    }

    override fun getItemViewType(position: Int): Int {
        return if (getRealData(position).id.isEmpty())  AD else NORMAL
    }

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int
    ): BannerViewHolder {
        if (viewType == AD) {
            val itemView = FrameLayout(parent.context)
            itemView.layoutParams = ViewGroup.LayoutParams(-1, -1)
            val adView = FrameLayout(parent.context)
            adView.layoutParams = FrameLayout.LayoutParams(-1, -2).apply {
                gravity = Gravity.CENTER
            }
            itemView.addView(adView)
            val admParams = ADMParams.Builder()
                .slotId(ADConfig.BANNER_ID)
                .layout(adView)
                .width(parent.width)
                .height(0)
                .build()
            val adBannerAd = ADBannerAd(parent.context as Activity, admParams, object : ADBannerAdListener {
                override fun onADLoadStart() {}

                override fun onADShow() {}

                override fun onADLoadedFail(code: Int, error: String) {
                    Log.e("jdy", "onBannerADLoadedFail: $code, error: $error")
                    adView.setBackgroundResource(R.drawable.ic_error)
                }

                override fun onADClick() {
                    Log.d("jdy", "onADClick")
                }

                override fun onADLoadSuccess() {

                }

                override fun onADClose() {
                }
            })
            adBannerAd.loadAD()
            return AdViewHolder(itemView)
        } else {
            val binding =
                ItemListBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return NormalViewHolder(binding)
        }
    }

    override fun onBindView(
        holder: BannerViewHolder,
        data: BannerItem,
        position: Int,
        size: Int
    ) {
        if (holder is NormalViewHolder) {
            val binding = holder.binding
            binding.bannerTitle.text = data.name
            binding.bannerContent.isVisible = !data.desc.isNullOrBlank()
            binding.bannerContent.text = data.desc
            binding.bannerImg.setRadius(30)
            Glide.with(holder.itemView)
                .load(data.cover)
                .into(binding.bannerImg)
        }
    }

    sealed class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class NormalViewHolder(val binding: ItemListBannerBinding): BannerViewHolder(binding.root)

    class AdViewHolder(adView: View): BannerViewHolder(adView)
}