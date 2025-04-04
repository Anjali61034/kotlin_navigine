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
    private var locationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get locationId from arguments
        arguments?.let {
            locationId = it.getInt("locationId", 0)
        }
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
            val locationManager = NavigineSdkManager.locationManager

            // Add location listener
            locationManager.addLocationListener(object : LocationListener() {
                override fun onLocationLoaded(location: Location) {
                    activity?.runOnUiThread {
                        // Get location view controller
                        val controller = locationView.locationWindow

                        // Set sublocation ID (assuming first sublocation)
                        if (location.sublocations.isNotEmpty()) {
                            controller.setSublocationId(location.sublocations[0].id)
                        }

                        // Start navigation
                        startNavigation()
                    }
                }

                override fun onLocationFailed(error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Failed to load location: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onDownloadProgress(progress: Int, total: Int) {
                    Log.d("Navigine", "Download progress: $progress/$total")
                }
            })

            // Set location ID to load
            locationManager.locationId
        } catch (e: Exception) {
            Log.e("Navigine", "Error loading location: ${e.message}")
        }
    }

    private fun startNavigation() {
        try {
            val navigationManager = NavigineSdkManager.navigationManager

            // Add position listener
            navigationManager.addPositionListener(object : PositionListener() {
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
            })

            // Start navigation
            navigationManager.startLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error starting navigation: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop navigation
        try {
            NavigineSdkManager.navigationManager.stopLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error stopping navigation: ${e.message}")
        }
    }
}