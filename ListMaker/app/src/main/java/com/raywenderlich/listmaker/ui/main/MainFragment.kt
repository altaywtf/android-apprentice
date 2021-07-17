package com.raywenderlich.listmaker.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.listmaker.models.TaskList
import com.raywenderlich.listmaker.databinding.MainFragmentBinding

class MainFragment: Fragment(), ListSelectionRecyclerViewAdapter.ListSelectionRecyclerViewClickListener {
    lateinit var clickListener: MainFragmentInteractionListener

    interface MainFragmentInteractionListener {
        fun listItemTapped(list: TaskList)
    }

    private lateinit var binding: MainFragmentBinding

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.listsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity()
        viewModel = ViewModelProvider(
            activity,
            MainViewModelFactory(PreferenceManager.getDefaultSharedPreferences(activity))
        ).get(MainViewModel::class.java)

        val recyclerViewAdapter = ListSelectionRecyclerViewAdapter(viewModel.lists, this)
        binding.listsRecyclerview.adapter = recyclerViewAdapter

        viewModel.onListAdded = {
            recyclerViewAdapter.listsUpdated()
        }
    }

    // ListSelectionRecyclerViewClickListener
    override fun listItemClicked(list: TaskList) {
        clickListener.listItemTapped(list)
    }
}