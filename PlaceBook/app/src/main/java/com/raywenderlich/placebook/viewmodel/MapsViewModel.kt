package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

class MapsViewModel(application: Application): AndroidViewModel(application) {
    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()

        bookmarkRepo.addBookmark(bookmark)
        image?.let {
            bookmark.setImage(it, getApplication())
        }
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkView {
        return BookmarkView(
            id = bookmark.id,
            location = LatLng(bookmark.latitude, bookmark.longitude),
            name = bookmark.name,
            phone = bookmark.phone
        )
    }

    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) {
            it.map { bookmark -> bookmarkToBookmarkView(bookmark) }
        }
    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }

        return bookmarks
    }

    data class BookmarkView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        val phone: String = ""
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFileName(it))
        }
    }
}