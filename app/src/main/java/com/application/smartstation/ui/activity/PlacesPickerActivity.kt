package com.application.smartstation.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityPlacesPickerBinding
import com.application.smartstation.ui.adapter.NearbyPlacesAdapter
import com.application.smartstation.ui.model.Place
import com.application.smartstation.util.PermissionsUtil
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.PlacesPickerViewModel
import com.application.smartstation.viewmodel.PlacesPickerViewModelFactory
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class PlacesPickerActivity : ScopedActivity(), OnMapReadyCallback,NearbyPlacesAdapter.OnClickListener {

    val binding: ActivityPlacesPickerBinding by viewBinding()
    private lateinit var viewModel: PlacesPickerViewModel
    private lateinit var mMap: GoogleMap
    private var markerOptions: MarkerOptions? = null
    private var mMarker: Marker? = null
    private val places = mutableListOf<Place>()
    private lateinit var mAdapter: NearbyPlacesAdapter

    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val REQUIRED_SDK_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_picker)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        viewModel = ViewModelProvider(this, PlacesPickerViewModelFactory(this, this))
            .get(PlacesPickerViewModel::class.java)


        val mapFragment = fragmentManager
            .findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)


        setupAdapter()

        viewModel.currentLocationLiveData.observe(this) {
            if (mMarker == null) {
                markerOptions = MarkerOptions().position(it!!)
                if (mMap != null)
                    mMarker = mMap.addMarker(markerOptions)

            }


            mMarker?.position = it


            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }


        viewModel.nearbyPlacesLiveData.observe(this, Observer {
//            if (binding.swNearBy.isChecked) {
//                places.clear()
//                places.addAll(it)
//                mAdapter.notifyDataSetChanged()
//            }
        })
        viewModel.showLocationDialogLiveData.observe(this, Observer {
            enableGps()
        })

//        binding.swNearBy.setOnCheckedChangeListener { switch, isChecked ->
//            if (isChecked) {
//                if (!PermissionsUtil.hasLocationPermissions(this@PlacesPickerActivity)) {
//                    switch.toggle()
//                    Toast.makeText(this@PlacesPickerActivity, R.string.missing_permissions, Toast.LENGTH_SHORT).show()
//                } else {
//                    viewModel.markerMoved(mMap.cameraPosition.target)
//                }
//            } else {
//                places.clear()
//                mAdapter.notifyDataSetChanged()
//            }
//        }

        mAdapter.onClickListener = this

    }

    private fun enableGps() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val result =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener {
            try {
                val response = it.getResult(ApiException::class.java)

                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    // Location settings are not satisfied. But could be fixed by showing the
                    // user a dialog.
                    try {
                        // Cast to a resolvable exception.
                        val resolvable = exception as ResolvableApiException
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        resolvable.startResolutionForResult(
                            this,
                            LocationRequest.PRIORITY_HIGH_ACCURACY)
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    } catch (e: ClassCastException) {
                        // Ignore, should be an impossible error.
                    }

                }
            }
        }
    }

    private fun setupAdapter() {
        mAdapter = NearbyPlacesAdapter(this, places)
//        binding.placeBottom.rvPlaces.apply {
//            adapter = mAdapter
//            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@PlacesPickerActivity)
//        }
    }

    private fun setOnClickListener() {
        binding.getLocation.setOnClickListener {
            checkPermissions()
        }

//        binding.txtSelectThisLocation.setOnClickListener {
//            showDialog()
//        }
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map!!
        mMap.setOnCameraMoveListener {
            mMarker?.position = mMap.cameraPosition.target
//            if (binding.swNearBy.isChecked) {
//                viewModel.markerMoved(map.cameraPosition.target)
//            }
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.size == REQUIRED_SDK_PERMISSIONS.size) {
            // request all missing permissions
            val permissions = missingPermissions
                .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            if (resultCode == RESULT_OK) {
                // All required changes were successfully made
                viewModel.getCurrentLocation()
//                if (binding.swNearBy.isChecked) {
//                    viewModel.markerMoved(mMap.cameraPosition.target)
//                }

            } else {
                //GPS NOT ENABLED BY USER
            }
        }
    }

    override fun onClick(view: View, place: Place) {
        showDialog(place)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                //check if nothing was granted, if so exit the activity
                if (grantResults.all { it != PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, R.string.missing_permissions, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
                // all permissions were granted
                viewModel.getCurrentLocation()

            }
        }
    }

    private fun showDialog(place: Place? = null) {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setTitle(getString(R.string.user_this_location))

            if (place != null) {
                val message = "${place.name} \n ${place.address}"
                setMessage(message)
            }

            setNegativeButton(R.string.change_location, null)
            setPositiveButton(R.string.select) { _, _ ->
                val data = Intent()
                if (place != null) {
                    data.putExtra(Place.EXTRA_PLACE, place)
                } else {
                    data.putExtra(Place.EXTRA_PLACE, Place("", "", "", mMap.cameraPosition.target))
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }

            dialog.show()
        }
    }


}