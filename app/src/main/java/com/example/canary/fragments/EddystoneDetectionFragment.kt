package com.example.canary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.navigine.idl.java.MeasurementListener
import com.navigine.idl.java.SignalMeasurement
import com.example.canary.R
import com.example.canary.NavigineSdkManager
import com.example.canary.viewmodel.SharedViewModel
import java.util.*

class EddystoneDetectionFragment : Fragment() {

    companion object {
        const val DEBUG_TIMEOUT_NO_SIGNAL = 5000L
    }

    private var viewModel: SharedViewModel? = null

    // Views
    private var mListViewEddystone: RecyclerView? = null

    // Data
    private val eddystoneEntries = mutableListOf<SignalMeasurement>()

    // Adapter
    private var debugEddystoneAdapter: DebugAdapterEddystone? = null

    // Listener
    private var measurementListener: MeasurementListener? = null

    // Timestamps
    private var timestampEddystones = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        initAdapter()
        initListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eddystone_detection, container, false)
        initViews(view)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        addListener()
    }

    override fun onPause() {
        super.onPause()
        removeListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun initAdapter() {
        debugEddystoneAdapter = DebugAdapterEddystone()
    }

    private fun initListener() {
        measurementListener = object : MeasurementListener() {

            override fun onSensorMeasurementDetected(
                hashMap: HashMap<com.navigine.idl.java.SensorType, com.navigine.idl.java.SensorMeasurement>
            ) {
                // Not needed for Eddystone detection
            }

            override fun onSignalMeasurementDetected(hashMap: HashMap<String, SignalMeasurement>) {
                // Clear previous entries
                eddystoneEntries.clear()

                // Filter only Eddystone signals
                for (signal in hashMap.values) {
                    if (signal.type == com.navigine.idl.java.SignalType.EDDYSTONE) {
                        eddystoneEntries.add(signal)
                    }
                }

                // Sort by RSSI (strongest first)
                eddystoneEntries.sortWith { result1, result2 ->
                    result2.rssi.compareTo(result1.rssi)
                }

                // Update UI
                activity?.runOnUiThread {
                    if (eddystoneEntries.isNotEmpty()) {
                        timestampEddystones = System.currentTimeMillis()
                        debugEddystoneAdapter?.submit(eddystoneEntries)
                    } else if (System.currentTimeMillis() - timestampEddystones >= DEBUG_TIMEOUT_NO_SIGNAL) {
                        // Clear list after timeout
                        debugEddystoneAdapter?.submit(emptyList())
                    }
                }
            }
        }
    }

    private fun initViews(view: View) {
        mListViewEddystone = view.findViewById(R.id.recyclerview_eddystone)
    }

    private fun setupRecyclerView() {
        mListViewEddystone?.let { recyclerView ->
            // Set LayoutManager
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Add divider decoration
            val divider = DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
            recyclerView.addItemDecoration(divider)

            // Set adapter
            recyclerView.adapter = debugEddystoneAdapter
        }
    }

    private fun addListener() {
        measurementListener?.let {
            NavigineSdkManager.measurementManager.addMeasurementListener(it)
        }
    }

    private fun removeListener() {
        measurementListener?.let {
            NavigineSdkManager.measurementManager.removeMeasurementListener(it)
        }
    }
}