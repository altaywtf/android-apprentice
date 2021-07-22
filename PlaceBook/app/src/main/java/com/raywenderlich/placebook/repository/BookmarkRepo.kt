package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(val context: Context) {
    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    private val categoryMap = buildCategoryMap()
    private var categoryResourceMap = buildCategoryResourceMap()

    fun createBookmark() = Bookmark()

    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId // why?
        return newId
    }

    fun getLiveBookmark(bookmarkId: Long) = bookmarkDao.loadLiveBookmark(bookmarkId)

    fun getBookmark(bookmarkId: Long) = bookmarkDao.loadBookmark(bookmarkId)

    fun updateBookmark(bookmark: Bookmark) = bookmarkDao.updateBookmark(bookmark)

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() = bookmarkDao.loadAll()

    private fun buildCategoryMap() = hashMapOf(
        Place.Type.BAKERY to "Restaurant",
        Place.Type.BAR to "Restaurant",
        Place.Type.CAFE to "Restaurant",
        Place.Type.FOOD to "Restaurant",
        Place.Type.RESTAURANT to "Restaurant",
        Place.Type.MEAL_DELIVERY to "Restaurant",
        Place.Type.MEAL_TAKEAWAY to "Restaurant",
        Place.Type.GAS_STATION to "Gas",
        Place.Type.CLOTHING_STORE to "Shopping",
        Place.Type.DEPARTMENT_STORE to "Shopping",
        Place.Type.FURNITURE_STORE to "Shopping",
        Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
        Place.Type.HARDWARE_STORE to "Shopping",
        Place.Type.HOME_GOODS_STORE to "Shopping",
        Place.Type.JEWELRY_STORE to "Shopping",
        Place.Type.SHOE_STORE to "Shopping",
        Place.Type.SHOPPING_MALL to "Shopping",
        Place.Type.STORE to "Shopping",
        Place.Type.LODGING to "Lodging",
        Place.Type.ROOM to "Lodging"
    )

    fun placeTypeToCategory(placeType: Place.Type): String {
        if (categoryMap.containsKey(placeType)) {
            return categoryMap[placeType].toString()
        }

        return "Other"
    }

    private fun buildCategoryResourceMap() = hashMapOf(
        "Gas" to R.drawable.ic_gas,
        "Lodging" to R.drawable.ic_lodging,
        "Other" to R.drawable.ic_other,
        "Restaurant" to R.drawable.ic_restaurant,
        "Shopping" to R.drawable.ic_shopping
    )

    fun getCategoryResourceId(bookmarkCategory: String): Int? {
        return categoryResourceMap[bookmarkCategory]
    }

    val categories: List<String>
        get() = ArrayList(categoryResourceMap.keys)
}