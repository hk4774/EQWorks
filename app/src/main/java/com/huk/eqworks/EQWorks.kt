package com.huk.eqworks

import EQMobileWorkSample.Library
import android.app.Application
import com.github.simonpercic.oklog3.OkLogInterceptor
import okhttp3.OkHttpClient


class EQWorks : Application() {

    override fun onCreate() {
        super.onCreate()
        Library.instance.setUp(getOkHttpClient())
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(OkLogInterceptor.builder().build())
            .build()
    }
}