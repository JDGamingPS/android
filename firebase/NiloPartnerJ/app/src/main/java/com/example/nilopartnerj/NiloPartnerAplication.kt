package com.example.nilopartnerj

import android.app.Application
import com.example.nilopartnerj.fcm.VolleyHelper

class NiloPartnerAplication: Application() {
    companion object{
        lateinit var volleyHelper: VolleyHelper
    }

    override fun onCreate() {
        super.onCreate()

        volleyHelper = VolleyHelper.obtenerInstancia(this)
    }
}