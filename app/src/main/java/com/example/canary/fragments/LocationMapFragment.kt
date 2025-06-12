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
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
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
        // Inflate layout
        val view = inflater.inflate(R.layout.fragment_location_map, container, false)

        // Check if back button exists; if not, add programmatically
        val backButton = view.findViewById<Button?>(R.id.back_button)
        if (backButton == null) {
            val newBackButton = Button(requireContext()).apply {
                id = View.generateViewId()
                text = "Back"
                setBackgroundResource(android.R.drawable.ic_menu_revert)
            }
            val parentView = view as? ViewGroup
            parentView?.addView(newBackButton, 0)
            newBackButton.setOnClickListener { goBack() }
        } else {
            backButton.setOnClickListener { goBack() }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationView = view.findViewById(R.id.location_view)
        sublocationListView = view.findViewById(R.id.sublocation_list_view)
        sublocationAdapter = SublocationAdapter(requireContext(), R.layout.list_item_sublocation)
        sublocationListView.adapter = sublocationAdapter

        // Set click for sublocation list
        sublocationListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val sublocation = sublocationAdapter.getItem(position)
                locationView.locationWindow.setSublocationId(sublocation.id)
            }

        navController = Navigation.findNavController(view)

        // Initialize the button to navigate to EddystoneDetectionFragment
        val eddystoneBtn = view.findViewById<Button>(R.id.eddystone_button)
        eddystoneBtn.setOnClickListener {
            navController.navigate(R.id.eddystoneDetectionFragment)
        }

        // Load location
        loadLocation()

        // Handle hardware back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBack()
                }
            }
        )
    }

    private fun goBack() {
        Log.d("Navigine", "Custom back button pressed")
        try {
            if (!navController.popBackStack()) {
                navController.navigate(R.id.locationListFragment)
            }
        } catch (e: Exception) {
            Log.e("Navigine", "Error navigating back: ${e.message}")
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadLocation() {
        try {
            cleanupListeners()
            val locationManager = NavigineSdkManager.locationManager

            locationListener = object : LocationListener() {
                override fun onLocationLoaded(location: Location) {
                    activity?.runOnUiThread {
                        currentLocation = location
                        val controller = locationView.locationWindow
                        Log.d("Navigine", "Location loaded, applying sublocation settings")
                        if (location.sublocations.isNotEmpty()) {
                            val sublocations = ArrayList(location.sublocations)
                            sublocationAdapter.submit(sublocations)
                            if (sublocationId != -1) {
                                controller.setSublocationId(sublocationId)
                            } else {
                                controller.setSublocationId(location.sublocations[0].id)
                            }
                        }
                        startNavigation()
                    }
                }

                override fun onLocationFailed(locationId: Int, error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to load location: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLocationUploaded(locationId: Int) {
                    Log.d("Navigine", "Location uploaded: $locationId")
                }
            }
            locationManager.addLocationListener(locationListener)
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
            positionListener = object : PositionListener() {
                override fun onPositionUpdated(position: Position) {
                    Log.d("Navigine", "Position updated")
                }

                override fun onPositionError(error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Position error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            navigationManager.addPositionListener(positionListener)
            navigationManager.startLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error starting navigation: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cleanupListeners() {
        try {
            positionListener?.let {
                NavigineSdkManager.navigationManager.removePositionListener(it)
                positionListener = null
            }
            locationListener?.let {
                NavigineSdkManager.locationManager.removeLocationListener(it)
                locationListener = null
            }
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