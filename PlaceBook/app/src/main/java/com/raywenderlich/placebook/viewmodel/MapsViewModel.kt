package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo

class MapsViewModel(application: Application): AndroidViewModel(application) {
    private val TAG = "MapsViewModel"
    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookMarkerView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let {
            bookmark.setImage(it, getApplication())
        }
    }

    private fun bookmarkToMarkerView(bookmark: Bookmark): BookMarkerView {
        return BookMarkerView(bookmark.id, LatLng(bookmark.latitude, bookmark.longitude))
    }

    private fun mapBookmarksToMarkerView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) {
            it.map { bookmark -> bookmarkToMarkerView(bookmark) }
        }
    }

    fun getBookMarkerViews(): LiveData<List<BookMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }

        return bookmarks
    }

    data class BookMarkerView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0)
    )
}