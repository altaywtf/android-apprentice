package com.raywenderlich.placebook.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.adapter.BookmarkListAdapter
import com.raywenderlich.placebook.databinding.ActivityMapsBinding
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mapsViewModel by viewModels<MapsViewModel>()
    private lateinit var dataBinding: ActivityMapsBinding

    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    private var markers = HashMap<Long, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupPlacesClient()
        setupLocationClient()
        setupToolbar()
        setupNavigationDrawer()
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
        createBookmarkObserver()
        getCurrentLocation()
    }

    private fun setupMapListeners() {
        map.setOnPoiClickListener { displayPoi(it)  }
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
        dataBinding.mainMapView.fab.setOnClickListener {
            searchAtCurrentLocation()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(dataBinding.mainMapView.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            dataBinding.drawerLayout,
            dataBinding.mainMapView.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        dataBinding.drawerViewMaps.bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        dataBinding.drawerViewMaps.bookmarkRecyclerView.adapter = bookmarkListAdapter
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
        val request = FetchPlaceRequest
            .builder(pointOfInterest.placeId, PLACE_FIELDS)
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
        marker?.showInfoWindow()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is PlaceInfo -> {
                val placeInfo = marker.tag as PlaceInfo

                if (placeInfo.place != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }

                marker.remove()
            }

            is MapsViewModel.BookmarkView -> {
                marker.hideInfoWindow()
                val bookmarkView = (marker.tag as MapsViewModel.BookmarkView)
                bookmarkView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun updateMapLocation(location: Location) {
        var latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        dataBinding.drawerLayout.closeDrawer(dataBinding.drawerViewMaps.drawerView)
        val marker = markers[bookmark.id]

        marker?.showInfoWindow()

        var location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude

        updateMapLocation(location)
    }

    // presenting bookmarks in the db
    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkView): Marker? {
        val marker = map.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(bookmark.categoryResourceId?.let { BitmapDescriptorFactory.fromResource(it) })
                .alpha(0.8f)
        )

        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }
        return marker
    }

    private fun displayAllBookmarks(bookmarkViews: List<MapsViewModel.BookmarkView>) {
        bookmarkViews.forEach { bookmarkView -> addPlaceMarker(bookmarkView) }
    }

    private fun createBookmarkObserver() {
        mapsViewModel.getBookmarkViews()?.observe(this, {
            map.clear()
            markers.clear()

            it?.let { it ->
                displayAllBookmarks(it)
                bookmarkListAdapter.setBookmarkData(it)
            }
        })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun searchAtCurrentLocation() {
        val bounds = RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)

        try {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, PLACE_FIELDS)
                .setLocationBias(bounds)
                .build(this)

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
            Toast
                .makeText(this, "Problems searching", Toast.LENGTH_LONG)
                .show()
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast
                .makeText(this, "Google Play Services is unavailable", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapLocation(location)
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    companion object {
        const val EXTRA_BOOKMARK_ID = "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
        private const val TAG = "MapsActivity"
        private val PLACE_FIELDS = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES
        )
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)
}