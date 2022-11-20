package com.example.nilopartnerj.fcm

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleyHelper(contexto : Context) {

    companion object{
        @Volatile
        private var INSTANCIA : VolleyHelper? = null

        fun obtenerInstancia(contexto: Context) = INSTANCIA ?: synchronized(this){
            INSTANCIA ?: VolleyHelper(contexto).also { INSTANCIA = it }
        }
    }

    val colaRespuestas: RequestQueue by lazy {
        Volley.newRequestQueue(contexto.applicationContext)
    }

    fun <T> adicionarAlaColaDeRespuestas(req: Request<T>){
        colaRespuestas.add(req)
    }
}