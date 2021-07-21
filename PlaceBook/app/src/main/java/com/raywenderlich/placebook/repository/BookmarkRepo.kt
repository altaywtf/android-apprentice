package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(context: Context) {
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()

    fun createBookmark() = Bookmark()

    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId // why?
        return newId
    }

    fun getLiveBookmark(bookmarkId: Long) = bookmarkDao.loadLiveBookmark(bookmarkId)

    fun getBookmark(bookmarkId: Long) = bookmarkDao.loadBookmark(bookmarkId)

    fun updateBookmark(bookmark: Bookmark) = bookmarkDao.updateBookmark(bookmark)

    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }
}