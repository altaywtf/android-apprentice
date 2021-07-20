package com.raywenderlich.placebook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.databinding.ActivityMapsBinding
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapsBinding

    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mapsViewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupPlacesClient()
        setupLocationClient()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkMarkerObserver()
        getCurrentLocation()
    }

    private fun setupMapListeners() {
        map.setOnPoiClickListener { displayPoi(it)  }
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun getCurrentLocation() {
        val locationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
        } else {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16.0f))
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS
        )

        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener {
               displayPoiGetPhotoStep(it.place)
            }
            .addOnFailureListener {
                if (it is ApiException) {
                    Log.e(TAG, "Place not found: ${it.message}, statusCode: ${it.statusCode}")
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas?.get(0) ?: return displayPoiDisplayStep(place, null)

        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
            .setMaxWidth((resources.getDimensionPixelOffset(R.dimen.default_image_width)))
            .setMaxHeight((resources.getDimensionPixelOffset(R.dimen.default_image_height)))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener {
                displayPoiDisplayStep(place, it.bitmap)
            }
            .addOnFailureListener {
                if (it is ApiException) {
                    Log.e(TAG, "Photo not found: ${it.message}, statusCode: ${it.statusCode}")
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(place.latLng as LatLng)
                .title(place.name)
                .snippet(place.phoneNumber)
        )

        marker?.tag = PlaceInfo(place, photo)
    }

    private fun handleInfoWindowClick(marker: Marker) {
        val placeInfo = marker.tag as PlaceInfo

        if (placeInfo.place != null) {
            GlobalScope.launch {
                mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
            }
        }

        marker.remove()
    }

    // presenting bookmarks in the db
    private fun addPlaceMarker(bookmark: MapsViewModel.BookMarkerView): Marker? {
        val marker = map.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .alpha(0.8f)
        )

        marker.tag = bookmark
        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookMarkerView>) {
        bookmarks.forEach { bookmark -> addPlaceMarker(bookmark) }
    }

    private fun createBookmarkMarkerObserver() {
        mapsViewModel.getBookMarkerViews()?.observe(this, {
            map.clear()
            it?.let { displayAllBookmarks(it) }
        })
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}