package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.databinding.ContentBookmarkInfoBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(context: Activity): InfoWindowAdapter {
    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {
                binding.photo.setImageBitmap(((marker.tag as MapsActivity.PlaceInfo).image))
            }

            is MapsViewModel.BookMarkerView -> {
                val bookMarkView = marker.tag as MapsViewModel.BookMarkerView
            }
        }

        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""

        return binding.root
    }
}