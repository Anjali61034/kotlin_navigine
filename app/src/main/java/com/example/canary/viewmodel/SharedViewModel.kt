package com.example.canary.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.example.canary.NavigineSdkManager

class SharedViewModel : ViewModel() {

    val mLocation = MutableLiveData<Location?>(null)

    private val locationListener: LocationListener = object : LocationListener() {
        override fun onLocationLoaded(location: Location) {
            mLocation.postValue(location)
        }

        override fun onLocationFailed(code: Int, error: Error?) {
            // Optional: Log or handle the error if needed
        }

        override fun onLocationUploaded(locationId: Int) {
            // Optional: Do something if needed
        }
    }

    init {
        NavigineSdkManager.locationManager.addLocationListener(locationListener)
    }

    override fun onCleared() {
        super.onCleared()
        NavigineSdkManager.locationManager.removeLocationListener(locationListener)
    }
}
