package com.shopapp.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopapp.data.repository.BannerItem
import com.shopapp.databinding.ItemBannerBinding

class BannerAdapter(
    private val onBannerClick: (BannerItem) -> Unit = {}
) : ListAdapter<BannerItem, BannerAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BannerItem) {
            binding.tvBannerTitle.text = item.title
            binding.tvBannerSubtitle.text = item.subtitle
            try {
                binding.bannerRoot.setBackgroundColor(Color.parseColor(item.color))
            } catch (e: Exception) {
                binding.bannerRoot.setBackgroundColor(Color.parseColor("#2D6A4F"))
            }
            binding.root.setOnClickListener { onBannerClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<BannerItem>() {
        override fun areItemsTheSame(o: BannerItem, n: BannerItem) = o.id == n.id
        override fun areContentsTheSame(o: BannerItem, n: BannerItem) = o == n
    }
}
