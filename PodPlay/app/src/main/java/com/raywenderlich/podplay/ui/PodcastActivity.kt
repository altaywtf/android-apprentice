package com.raywenderlich.podplay.ui

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.databinding.ActivityPodcastBinding
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {
    private val searchViewModel by viewModels<SearchViewModel>()
    private val podcastViewModel by viewModels<PodcastViewModel>()

    private lateinit var binding: ActivityPodcastBinding
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRootView()
        setupToolbar()
        setupViewModels()
        setupPodcastList()
        addBackStackListener()
        handleIntent(intent)
    }

    private fun setupViewModels() {
        searchViewModel.itunesRepo = ItunesRepo(ItunesService.instance)
        podcastViewModel.podcastRepo = PodcastRepo(RssFeedService.instance)
    }

    private fun setupRootView() {
        binding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupPodcastList() {
        binding.podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        binding.podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            binding.podcastRecyclerView.context,
            layoutManager.orientation
        )
        binding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(null, this, this)
        binding.podcastRecyclerView.adapter = podcastListAdapter
    }

    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        if (supportFragmentManager.backStackEntryCount > 0) {
            binding.podcastRecyclerView.visibility = View.INVISIBLE
        }

        if (binding.podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }

        return true
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun performSearch(term: String) {
        showProgressBar()

        GlobalScope.launch {
            val results = searchViewModel.searchPodcasts(term)

            withContext(Dispatchers.Main) {
                hideProgressBar()
                binding.toolbar.title = term
                podcastListAdapter.setData(results)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY) ?: return
                performSearch(query)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button), null)
            .create()
            .show()
    }

    private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

        if (podcastDetailsFragment == null) {
            podcastDetailsFragment = PodcastDetailsFragment.newInstance()
        }

        return podcastDetailsFragment
    }

    private fun showDetailsFragment() {
        val podcastDetailsFragment = createPodcastDetailsFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.podcastDetailsContainer, podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment")
            .commit()

        binding.podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        var feedUrl = podcastSummaryViewData?.feedUrl ?: return

        showProgressBar()
        GlobalScope.launch {
            val podcast = podcastViewModel.getPodcast(podcastSummaryViewData)

            withContext(Dispatchers.Main) {
                hideProgressBar()
                
                if (podcast == null) {
                    showError("Error loading feed $feedUrl")
                } else {
                    showDetailsFragment()
                }
            }
        }
    }

    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }
}
