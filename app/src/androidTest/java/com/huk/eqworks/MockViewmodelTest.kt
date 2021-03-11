package com.huk.eqworks

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.platform.app.InstrumentationRegistry
import com.huk.eqworks.ui.home.viewmodel.LocationViewModel
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.ref.WeakReference


@RunWith(JUnit4::class)
class MockViewmodelTest {

    lateinit var context: WeakReference<Context>
    lateinit var viewModel: LocationViewModel
    lateinit var coLocation: CoLocation
    lateinit var coGeocoder: CoGeocoder

    @Before
    fun setUp() {
        context = WeakReference(InstrumentationRegistry.getInstrumentation().targetContext)
        coLocation = CoLocation.from(context.get()!!)
        coGeocoder = CoGeocoder.from(context.get()!!)
        viewModel = LocationViewModel(coLocation, coGeocoder, context)
    }

    @Test
    fun testLocationAPI() {
        viewModel.sendLocation(getMockLocation(), object : APICallback() {
            override fun onResponse(isSuccess: Boolean) {
                assert(isSuccess)
            }
        })

        viewModel.sendLocation(getMockLocation2(), object : APICallback() {
            override fun onResponse(isSuccess: Boolean) {
                assert(isSuccess)
            }
        })
    }

    private fun getMockLocation(): Location {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = 1.0
        location.longitude = 2.0
        location.time = System.currentTimeMillis()

        return location
    }

    private fun getMockLocation2(): Location {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = 0.0
        location.longitude = 0.0

        return location
    }
}