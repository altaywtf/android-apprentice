package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.databinding.ContentBookmarkInfoBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(val context: Activity): InfoWindowAdapter {
    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""

        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {
                val image = (marker.tag as MapsActivity.PlaceInfo).image
                binding.photo.setImageBitmap(image)
            }

            is MapsViewModel.BookMarkerView -> {
                val image = (marker.tag as MapsViewModel.BookMarkerView).getImage(context)
                binding.photo.setImageBitmap(image)
            }
        }

        return binding.root
    }
}