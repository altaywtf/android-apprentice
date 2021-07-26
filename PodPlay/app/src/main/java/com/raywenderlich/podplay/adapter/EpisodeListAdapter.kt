package com.raywenderlich.podplay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.podplay.databinding.EpisodeItemBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class EpisodeListAdapter(
    private var episodeViewList: List<PodcastViewModel.EpisodeViewData>?
): RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

    inner class ViewHolder(binding: EpisodeItemBinding): RecyclerView.ViewHolder(binding.root) {
        var episodeViewData: PodcastViewModel.EpisodeViewData? = null
        val titleTextView: TextView = binding.titleView
        val descTextView: TextView = binding.descView
        val durationTextView: TextView = binding.durationView
        val releaseDateTextView: TextView = binding.releaseDateView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val episodeItemBinding = EpisodeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(episodeItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeViewList = episodeViewList ?: return
        val episodeView = episodeViewList[position]

        holder.episodeViewData = episodeView
        holder.titleTextView.text = episodeView.title
        holder.descTextView.text = episodeView.description
        holder.durationTextView.text = episodeView.duration
        holder.releaseDateTextView.text = episodeView.releaseDate.toString()
    }

    override fun getItemCount(): Int {
        return episodeViewList?.size ?: 0
    }
}