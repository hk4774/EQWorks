package com.huk.eqworks.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.huk.eqworks.R
import com.huk.eqworks.ui.home.viewmodel.LocationViewModel
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import kotlinx.android.synthetic.main.fragment_location.*
import kotlinx.android.synthetic.main.fragment_location.view.*
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LocationFragment : Fragment() {

    companion object {
        private val DATE_FORMAT = DateFormat.getDateTimeInstance()
        private const val REQUEST_SHOW_SETTINGS = 123
    }

    private var lastLocation: Location? = getDefaultLocation()

    private val noGPSDialog: AlertDialog by lazy {
        return@lazy AlertDialog.Builder(context)
            .setTitle(R.string.gps_not_found_title) // GPS not found
            .setMessage(R.string.gps_not_found_message) // Want to enable?
            .setCancelable(false)
            .setPositiveButton(
                R.string.enable
            ) { dialogInterface, i ->
                this@LocationFragment.startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                )
            }.create()
    }
    private val permissionSettingDialog: AlertDialog by lazy {
        return@lazy AlertDialog.Builder(this@LocationFragment.requireActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.permission_required)
            .setCancelable(false)
            .setPositiveButton(
                R.string.alert_dialog_ok,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    this.requireContext().openAppSystemSettings()
                }
            )
            .setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialogInterface.dismiss()
                    this@LocationFragment.requireActivity().finishAffinity()
                }
                true
            }
            .create()
    }

    private val viewModel: LocationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                LocationViewModel(
                    CoLocation.from(this@LocationFragment.requireContext()),
                    CoGeocoder.from(this@LocationFragment.requireContext()),
                    WeakReference<Context>(this@LocationFragment.requireActivity().applicationContext)
                ) as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.send_location.setOnClickListener {
            it.context?.let {
                lastLocation?.let {
                    viewModel.sendLocation(it)
                }
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@LocationFragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this@LocationFragment.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { //Can add more as per requirement
            askPermission()
        } else {
            setup()
        }
    }

    private fun askPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_SHOW_SETTINGS
        )
    }

    private fun setup() {
        if (isLocationEnabled(this@LocationFragment.requireContext()) == false) {
            if (noGPSDialog.isShowing) Log.d(
                this@LocationFragment.javaClass.simpleName,
                "Already showing"
            ) else noGPSDialog.show()
            return
        }

        send_location.isEnabled = true

        lifecycle.addObserver(viewModel)
        viewModel.locationUpdates.observe(
            this@LocationFragment.requireActivity(),
            this::onLocationUpdate
        )
        viewModel.addressUpdates.observe(
            this@LocationFragment.requireActivity(),
            this::onAddressUpdate
        )
        viewModel.resolveSettingsEvent.observe(this@LocationFragment.requireActivity()) {
            it.resolve(
                this@LocationFragment.requireActivity(),
                REQUEST_SHOW_SETTINGS
            )
        }
    }

    private fun detachObserver() {
        lifecycle.removeObserver(viewModel)
        viewModel.locationUpdates.removeObservers(this@LocationFragment.requireActivity())
        viewModel.addressUpdates.removeObservers(this@LocationFragment.requireActivity())
        viewModel.resolveSettingsEvent.removeObservers(this@LocationFragment.requireActivity())
    }

    fun isLocationEnabled(context: Context): Boolean? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServicesAvailable()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    override fun onDestroy() {
        detachObserver()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SHOW_SETTINGS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setup()
            } else {
                //Now further we check if used denied permanently or not
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@LocationFragment.requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this@LocationFragment.requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    // User has denied permission but not permanently
                    askPermission()
                } else {
                    // Permission denied permanently.
                    showDialogForPermissionSettings()
                }
            }
        }
    }

    private fun showDialogForPermissionSettings() {
        if (permissionSettingDialog.isShowing) Log.d(
            this@LocationFragment.javaClass.simpleName,
            "Already showing"
        ) else permissionSettingDialog.show()
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this@LocationFragment.context)

        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this@LocationFragment.activity, status, 1).show()
            } else {
                view?.rootView?.let {
                    Snackbar.make(
                        it,
                        "Google Play Services unavailable. This app will not work",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
        }
    }

    private fun onLocationUpdate(location: Location?) {
        location_data?.let {
            location?.let { lastLocation = it }
            it.text = location?.run { "Current Location: $latitude, $longitude" }
                ?: getString(R.string.default_location)
        }
    }

    private fun onAddressUpdate(address: Address?) {
    }

    fun Context.openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }

    private fun getDefaultLocation(): Location {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = 0.0
        location.longitude = 0.0
        location.time = System.currentTimeMillis()

        return location
    }

}