package com.example.canary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.canary.adapters.SublocationAdapter
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.idl.java.Sublocation
import com.navigine.view.LocationView

class LocationMapFragment : Fragment() {

    private lateinit var locationView: LocationView
    private lateinit var sublocationListView: ListView
    private var locationId: Int = 1890
    private var sublocationId: Int = -1
    private lateinit var navController: NavController
    private lateinit var sublocationAdapter: SublocationAdapter<Sublocation>
    private var currentLocation: Location? = null

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
        // Inflate with the existing layout
        val view = inflater.inflate(R.layout.fragment_location_map, container, false)

        // Check if a back button already exists in the layout, if not, we'll add one programmatically
        val backButton = view.findViewById<Button>(R.id.back_button)
        if (backButton == null) {
            // Create a back button programmatically
            val newBackButton = Button(requireContext())
            newBackButton.id = View.generateViewId()
            newBackButton.text = "Back"

            // Style the button as needed
            newBackButton.setBackgroundResource(android.R.drawable.ic_menu_revert)

            // Add it to an appropriate parent in your layout
            val parentView = view as? ViewGroup
            parentView?.addView(newBackButton, 0) // Add at top

            // Set click listener
            newBackButton.setOnClickListener {
                goBack()
            }
        } else {
            // Use the existing back button
            backButton.setOnClickListener {
                goBack()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationView = view.findViewById(R.id.location_view)

        // Initialize the sublocation list view (you'll need to add this to your layout)
        sublocationListView = view.findViewById(R.id.sublocation_list_view)
        sublocationAdapter = SublocationAdapter(requireContext(), R.layout.list_item_sublocation)
        sublocationListView.adapter = sublocationAdapter

        // Set click listener for sublocation selection
        sublocationListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val sublocation = sublocationAdapter.getItem(position)
            locationView.locationWindow.setSublocationId(sublocation.id)

        }

        navController = Navigation.findNavController(view)

        // Load the selected location
        loadLocation()
    }

    private fun goBack() {
        Log.d("Navigine", "Custom back button pressed")
        try {
            // First try normal back stack navigation
            if (!navController.popBackStack()) {
                // If that doesn't work, navigate directly to LocationListFragment
                navController.navigate(R.id.locationListFragment)
            }
        } catch (e: Exception) {
            Log.e("Navigine", "Error navigating back: ${e.message}")
            // Last resort - use FragmentManager directly
            requireActivity().supportFragmentManager.popBackStack()
        }
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
                        // Store the location for reference
                        currentLocation = location

                        // Get location view controller
                        val controller = locationView.locationWindow

                        // Instead of reset, we'll set the location ID to ensure fresh loading
                        Log.d("Navigine", "Location loaded, applying sublocation settings")

                        // Update sublocation adapter with available sublocations
                        if (location.sublocations.isNotEmpty()) {
                            val sublocations = ArrayList(location.sublocations)
                            sublocationAdapter.submit(sublocations)

                            // Set sublocation ID (either from arguments or default to first)
                            if (sublocationId != -1) {
                                controller.setSublocationId(sublocationId)
                            } else {
                                controller.setSublocationId(location.sublocations[0].id)
                            }
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

    // Override back button press
    override fun onResume() {
        super.onResume()
        // Add a callback for the hardware back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBack()
                }
            }
        )
    }
}