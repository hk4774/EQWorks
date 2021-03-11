package com.huk.eqworks.ui.home.viewmodel

import EQMobileWorkSample.Library
import EQMobileWorkSample.LocationEvent
import EQMobileWorkSample.callback.Result
import EQMobileWorkSample.utility.logD
import Response
import android.content.Context
import android.location.Address
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.google.android.gms.location.LocationRequest
import com.huk.eqworks.APICallback
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class LocationViewModel(
    private val coLocation: CoLocation,
    private val coGeocoder: CoGeocoder,
    private val applicationContext: WeakReference<Context>
) : ViewModel(), LifecycleObserver {

    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        //.setSmallestDisplacement(1f)
        //.setNumUpdates(3)
        .setInterval(5000)
        .setFastestInterval(2500)


    private val mutableLocationUpdates: MutableLiveData<Location> = MutableLiveData()
    val locationUpdates: LiveData<Location> = mutableLocationUpdates
    val addressUpdates: LiveData<Address?> = locationUpdates.switchMap { location ->
        liveData { emit(coGeocoder.getAddressFromLocation(location)) }
    }

    private val mutableResolveSettingsEvent: MutableLiveData<CoLocation.SettingsResult.Resolvable> =
        MutableLiveData()
    val resolveSettingsEvent: LiveData<CoLocation.SettingsResult.Resolvable> =
        mutableResolveSettingsEvent

    private var locationUpdatesJob: Job? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        startLocationUpdatesAfterCheck()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    private fun startLocationUpdatesAfterCheck() {
        viewModelScope.launch {
            val settingsResult = coLocation.checkLocationSettings(locationRequest)
            when (settingsResult) {
                CoLocation.SettingsResult.Satisfied -> {
                    coLocation.getLastLocation()?.run(mutableLocationUpdates::postValue)
                    startLocationUpdates()
                }
                is CoLocation.SettingsResult.Resolvable -> mutableResolveSettingsEvent.postValue(
                    settingsResult
                )
                else -> { /* Ignore for now, we can't resolve this anyway */
                }
            }
        }
    }

    fun startLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            try {
                coLocation.getLocationUpdates(locationRequest).collect { location ->
                    Log.d("MainViewModel", "Location update received: $location")
                    mutableLocationUpdates.postValue(location)
                }
            } catch (e: CancellationException) {
                Log.e("MainViewModel", "Location updates cancelled", e)
            }
        }
    }

    fun sendLocation(location: Location, apiCallback: APICallback? = null) {

        Library.instance.log(
            LocationEvent(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                System.currentTimeMillis()
            ), object : Result<Response>() {
                override fun onResultReceived(result: Response) {
                    logD(result.toString())
                    Toast.makeText(
                        applicationContext.get(),
                        "Request successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    apiCallback?.onResponse(true)
                }

                override fun onErrorOccurred(error: Exception) {
                    error.message?.let { logD(it) }
                    apiCallback?.onResponse(false)
                }
            }
        )
    }

}