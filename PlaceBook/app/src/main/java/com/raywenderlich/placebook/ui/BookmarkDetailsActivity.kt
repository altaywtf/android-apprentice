package com.raywenderlich.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.ActivityBookmarkDetailsBinding
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel

class BookmarkDetailsActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoDialogOptionListener {
    private lateinit var databinding: ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }


    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let {
            val placeImage = it.getImage(this)
            placeImage?.let {
                databinding.imageViewPlace.setImageBitmap(placeImage)
                databinding.imageViewPlace.setOnClickListener {
                    replaceImage()
                }
            }
        }
    }

    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this, {
            it?.let {
                bookmarkDetailsView = it
                databinding.bookmarkDetailsView = it
                populateImageView()
            }
        })
    }

    private fun saveChanges() {
        val name = databinding.editTextName.text.toString()
        if (name.isEmpty()) { return }

        bookmarkDetailsView?.let {
            it.name = databinding.editTextName.text.toString()
            it.notes = databinding.editTextNotes.text.toString()
            it.address = databinding.editTextAddress.text.toString()
            it.phone = databinding.editTextPhone.text.toString()
            bookmarkDetailsViewModel.updateBookmark(it)
        }

        finish()
    }

    private fun replaceImage() {
        val photoOptionDialogFragment = PhotoOptionDialogFragment.newInstance(this)
        photoOptionDialogFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    override fun onCaptureClick() {
        TODO("Not yet implemented")
    }

    override fun onPickClick() {
        TODO("Not yet implemented")
    }
}
