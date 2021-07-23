package com.raywenderlich.podplay.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.databinding.FragmentPodcastDetailsBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class PodcastDetailsFragment: Fragment() {
    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var binding: FragmentPodcastDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPodcastDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    private fun updateUI() {
        val viewData = podcastViewModel.activePodcastViewData ?: return
        binding.feedTitleTextView.text = viewData.feedTitle
        binding.feedDescTextView.text = viewData.feedDesc
        activity?.let {
            Glide.with(it).load(viewData.imageUrl).into(binding.feedImageView)
        }
    }

    companion object {
        fun newInstance() = PodcastDetailsFragment()
    }
}
