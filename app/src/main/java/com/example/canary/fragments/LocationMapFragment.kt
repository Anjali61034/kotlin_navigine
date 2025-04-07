package com.example.canary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.view.LocationView

class LocationMapFragment : Fragment() {

    private lateinit var locationView: LocationView
    private var locationId: Int = 1890
    private var sublocationId: Int = -1

    // Store listeners for proper cleanup
    private var locationListener: LocationListener? = null
    private var positionListener: PositionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get arguments
        arguments?.let {
            locationId = it.getInt("locationId", 1890)
            sublocationId = it.getInt("sublocationId", -1)
        }

        Log.d("Navigine", "LocationMapFragment onCreate with locationId: $locationId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationView = view.findViewById(R.id.location_view)

        // Load the selected location
        loadLocation()
    }

    private fun loadLocation() {
        try {
            // First clean up any existing listeners
            cleanupListeners()

            val locationManager = NavigineSdkManager.locationManager

            // Add location listener
            locationListener = object : LocationListener() {
                override fun onLocationLoaded(location: Location) {
                    activity?.runOnUiThread {
                        // Get location view controller
                        val controller = locationView.locationWindow

                        // Instead of reset, we'll set the location ID to ensure fresh loading
                        Log.d("Navigine", "Location loaded, applying sublocation settings")

                        // Set sublocation ID (either from arguments or default to first)
                        if (sublocationId != -1) {
                            controller.setSublocationId(sublocationId)
                        } else if (location.sublocations.isNotEmpty()) {
                            controller.setSublocationId(location.sublocations[0].id)
                        }

                        // Start navigation
                        startNavigation()
                    }
                }

                override fun onLocationFailed(locationId: Int, error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Failed to load location: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onLocationUploaded(locationId: Int) {
                    Log.d("Navigine", "Location uploaded: $locationId")
                }
            }

            // Add the listener
            locationManager.addLocationListener(locationListener)

            // Set location ID to load - this will trigger a fresh location load
            Log.d("Navigine", "Loading location with ID: $locationId")
            locationManager.locationId = locationId
        } catch (e: Exception) {
            Log.e("Navigine", "Error loading location: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startNavigation() {
        try {
            val navigationManager = NavigineSdkManager.navigationManager

            // Add position listener
            positionListener = object : PositionListener() {
                override fun onPositionUpdated(position: Position) {
                    // Position is updated, location view will be updated automatically
                    Log.d("Navigine", "Position updated")
                }

                override fun onPositionError(error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Position error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            // Add the listener
            navigationManager.addPositionListener(positionListener)

            // Start navigation
            navigationManager.startLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error starting navigation: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cleanupListeners() {
        try {
            // Remove position listener
            positionListener?.let {
                NavigineSdkManager.navigationManager.removePositionListener(it)
                positionListener = null
            }

            // Remove location listener
            locationListener?.let {
                NavigineSdkManager.locationManager.removeLocationListener(it)
                locationListener = null
            }

            // Stop navigation
            NavigineSdkManager.navigationManager.stopLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error cleaning up: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        cleanupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupListeners()
    }
}