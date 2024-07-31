package com.dongyu.movies.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dongyu.movies.R
import com.dongyu.movies.data.home.BannerItem
import com.dongyu.movies.databinding.ItemListBannerBinding
import com.dongyu.movies.event.OnItemClickListener
import com.dongyu.movies.utils.dp2px
import com.dongyu.movies.utils.getWindowHeight
import com.dongyu.movies.utils.getWindowWidth

class BannerFragment : Fragment() {

    private var _binding: ItemListBannerBinding? = null
    private val binding get() = _binding!!

    private val options = RequestOptions()
        .override(getWindowWidth(), 200.dp2px())
        // .transform(CenterCrop(), RoundedCorners(16))

     var onClickListener: OnClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemListBannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        val bannerItem = BundleCompat.getParcelable(arguments, "bannerItem", BannerItem::class.java)
            ?: return

        binding.bannerTitle.text = bannerItem.name
        binding.bannerContent.text = bannerItem.desc
        Glide.with(this)
            .load(bannerItem.cover)
            .placeholder(R.drawable.image_loading)
            .apply(options)
            .into(binding.bannerImg)
        binding.root.setOnClickListener(onClickListener)
    }
}