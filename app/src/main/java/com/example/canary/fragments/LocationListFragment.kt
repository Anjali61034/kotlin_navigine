package com.example.canary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.navigine.idl.java.LocationInfo
import com.navigine.idl.java.LocationListListener
import androidx.navigation.fragment.findNavController
import java.util.HashMap

class LocationListFragment : Fragment() {

    private lateinit var locationListView: ListView
    private val locationNames = mutableListOf<String>()
    private val locationIds = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationListView = view.findViewById(R.id.location_list_view)

        // Create adapter
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            locationNames
        )
        locationListView.adapter = adapter

        // Set item click listener
        locationListView.setOnItemClickListener { _, _, position, _ ->
            val locationId = locationIds[position]

            // Navigate to map fragment with locationId
            val bundle = Bundle().apply {
                putInt("locationId", locationId)
            }
            findNavController().navigate(R.id.action_locationList_to_locationMap, bundle)
        }

        // Load locations
        loadLocations()
    }

    private fun loadLocations() {
        try {
            val locationListManager = NavigineSdkManager.locationListManager

            // Create listener with the correct implementation
            val locationListListener = object : LocationListListener() {
                override fun onLocationListLoaded(idToLocationInfoMap: HashMap<Int, LocationInfo>) {
                    activity?.runOnUiThread {
                        // Clear previous data
                        locationNames.clear()
                        locationIds.clear()

                        // Add locations to the list
                        for ((id, locationInfo) in idToLocationInfoMap) {
                            locationNames.add(locationInfo.name)
                            locationIds.add(id)
                        }

                        // Update adapter
                        (locationListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                    }
                }

                override fun onLocationListFailed(error: Error) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Failed to load location list: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            // Add listener
            locationListManager.addLocationListListener(locationListListener)

            // Update location list
            locationListManager.updateLocationList()

        } catch (e: Exception) {
            Log.e("Navigine", "Error loading locations: ${e.message}")
            e.printStackTrace()
        }
    }
}