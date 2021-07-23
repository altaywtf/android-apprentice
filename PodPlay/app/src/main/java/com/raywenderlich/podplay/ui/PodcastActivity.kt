package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.SearchView
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.databinding.ActivityPodcastBinding
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.ItunesService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName
    private lateinit var binding: ActivityPodcastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRootView()
        setupToolbar()
    }

    private fun setupRootView() {
        binding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        val searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem?.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return true
    }

    private fun performSearch(term: String) {
        val itunesRepo = ItunesRepo(ItunesService.instance)

        GlobalScope.launch {
            val results = itunesRepo.searchByTerm(term)
            Log.i(TAG, "Results = ${results.body()}")
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
}
