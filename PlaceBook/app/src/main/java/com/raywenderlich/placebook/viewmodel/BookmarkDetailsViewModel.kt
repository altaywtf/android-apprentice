package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkDetailsViewModel(application: Application): AndroidViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkDetailsView(bookmarkId)
        }

        return bookmarkDetailsView
    }

    private fun bookmarkToBookmarkDetailsView(bookmark: Bookmark): BookmarkDetailsView {
        return BookmarkDetailsView(
            id = bookmark.id,
            name = bookmark.name,
            phone = bookmark.phone,
            address = bookmark.address,
            notes =  bookmark.notes,
        )
    }

    private fun mapBookmarkToBookmarkDetailsView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark) {
            bookmarkToBookmarkDetailsView(it)
        }
    }

    private fun bookmarkDetailsViewToBookmark(bookmarkDetailsView: BookmarkDetailsView): Bookmark? {
        val bookmark =  bookmarkDetailsView.id?.let {
            bookmarkRepo.getBookmark(it)
        }

        if (bookmark != null) {
            // bookmark.id = bookmarkDetailsView.id
            bookmark.name = bookmarkDetailsView.name
            bookmark.phone = bookmarkDetailsView.phone
            bookmark.address = bookmarkDetailsView.address
            bookmark.notes = bookmarkDetailsView.notes
        }

        return bookmark
    }

    fun updateBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        GlobalScope.launch {
            val bookmark = bookmarkDetailsViewToBookmark(bookmarkDetailsView)
            bookmark?.let { bookmarkRepo.updateBookmark(it) }
        }
    }


    data class BookmarkDetailsView(
        var id: Long? = null,
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var notes: String = ""
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFileName(it))
        }

        fun setImage(context: Context, image: Bitmap) {
            id?.let {
                ImageUtils.saveBitmapToFile(context, image, Bookmark.generateImageFileName(it))
            }
        }
    }
}