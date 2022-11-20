package com.example.nilopartnerj.fcm

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.nilopartnerj.Constantes
import com.example.nilopartnerj.NiloPartnerAplication
import org.json.JSONException
import org.json.JSONObject

class NotificacionServicioRemoto {
    fun enviarNotificacion(titulo: String, mensaje: String, tokens: String) {
        val params = JSONObject()
        params.put(Constantes.PARAM_METODO, Constantes.ENVIAR_NOTIFICACION)
        params.put(Constantes.PARAM_TITULO, titulo)
        params.put(Constantes.PARAM_MENSAJE, mensaje)
        params.put(Constantes.PARAM_TOKENS, tokens)

        val jsonObjetosRespuestas: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
            Constantes.NILO_PARTNER_RemoteService, params, Response.Listener { respuesta ->
                try {
                    val success = respuesta.getInt(Constantes.PARAM_SUCCESS)
                    Log.i("volley success", success.toString())
                    Log.i("respuesta: ",respuesta.toString())
                }catch (e: JSONException){
                    e.printStackTrace()
                    Log.e("Volley exception", e.localizedMessage)
                }

            }, Response.ErrorListener { error ->
                if (error.localizedMessage != null){
                    Log.e("Volley error", error.localizedMessage)
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val paramsHeaders = HashMap<String, String>()
                paramsHeaders["Content-Type"] = "application/json: charset=utf-8"
                return super.getHeaders()

            }
        }

        NiloPartnerAplication.volleyHelper.adicionarAlaColaDeRespuestas(jsonObjetosRespuestas)
    }
}