package com.raywenderlich.podplay.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.databinding.SearchItemBinding
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData

class PodcastListAdapter(
    private var podcastSummaryViewList: List<PodcastSummaryViewData>?,
    private val podcastListAdapterListener: PodcastListAdapterListener,
    private val parentActivity: Activity
): RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {
    interface PodcastListAdapterListener {
        fun onShowDetails(podcastSummaryViewData: PodcastSummaryViewData)
    }

    inner class ViewHolder(
        binding: SearchItemBinding,
        private val podcastListAdapterListener: PodcastListAdapterListener
    ): RecyclerView.ViewHolder(binding.root) {
        var podcastSummaryViewData: PodcastSummaryViewData? = null
        val nameTextView = binding.podcastNameTextView
        val lastUpdatedTextView = binding.podcastLastUpdatedTextView
        val podcastImageView = binding.podcastImage

        init {
            binding.searchItem.setOnClickListener {
                podcastSummaryViewData?.let {
                    podcastListAdapterListener.onShowDetails(it)
                }
            }
        }
    }

    fun setData(podcastSummaryViewData: List<PodcastSummaryViewData>) {
        podcastSummaryViewList = podcastSummaryViewData
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val searchItemBinding = SearchItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(searchItemBinding, podcastListAdapterListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listData = podcastSummaryViewList ?: return
        val itemData = listData[position]

        holder.podcastSummaryViewData = itemData
        holder.nameTextView.text = itemData.name
        holder.lastUpdatedTextView.text = itemData.lastUpdated

        Glide.with(parentActivity)
            .load(itemData.imageUrl)
            .into(holder.podcastImageView)
    }

    override fun getItemCount(): Int {
        return podcastSummaryViewList?.size ?: 0
    }
}