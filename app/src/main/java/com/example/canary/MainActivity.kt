package com.example.canary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.canary.R.id.nav_host_fragment
import com.navigine.sdk.Navigine
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check permissions
        checkAndRequestPermissions()

        // Initialize Navigine SDK Manager
        initializeNavigine()

        // Setup navigation component
        val navHostFragment = supportFragmentManager
            .findFragmentById(nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun initializeNavigine() {
        try {
            // Initialize SDK Manager
            NavigineSdkManager.initialize(this)
            Log.d("Navigine", "SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("Navigine", "Error initializing SDK: ${e.message}")
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        // Add Bluetooth permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (!allGranted) {
                Log.w("Navigine", "Not all permissions were granted")
                // You might want to show a dialog explaining why the permissions are needed
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop navigation if needed
        try {
            NavigineSdkManager.navigationManager.stopLogRecording()
        } catch (e: Exception) {
            Log.e("Navigine", "Error stopping navigation: ${e.message}")
        }
    }
}