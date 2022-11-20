package com.example.niloj.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.niloj.Constantes
import com.example.niloj.MainActivity
import com.example.niloj.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage

class FCMServices : FirebaseMessagingService(){

    override fun onNewToken(nuevotoken: String) {
        super.onNewToken(nuevotoken)

        // funciona para generar un tokken y enviar una notificacion al usuario del token
        registrarNuevoTokenLocal(nuevotoken)
    }

    private fun registrarNuevoTokenLocal(nuevotoken: String){
        val preferencias = PreferenceManager.getDefaultSharedPreferences(this)

        preferencias.edit {
            putString(Constantes.PROP_TOKEN, nuevotoken)
                .apply()
        }

        Log.i("nuevo token", nuevotoken)

    }

    override fun onMessageReceived(mensajeRemoto: RemoteMessage) {
        super.onMessageReceived(mensajeRemoto)

        mensajeRemoto.notification?.let {
            enviarNotificacion(it)
        }
    }

    private fun enviarNotificacion(notificacion: RemoteMessage.Notification){
        val intent = Intent(this, MainActivity::class.java) // -> aqui se lanza el mainActivity cuando picamos la notificacion
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val intentPendiente= PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val canalId =  getString(R.string.notification_channel_id_default)
        val sonidoPorDefectoUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val constructorNotificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(notificacion.title)
            .setContentText(notificacion.body)
            .setAutoCancel(true)
            .setSound(sonidoPorDefectoUri)
            .setContentIntent(intentPendiente)

        val manejadorNotificacion = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val canal = NotificationChannel(canalId, getString(R.string.notification_channel_name_default), NotificationManager.IMPORTANCE_DEFAULT)
            manejadorNotificacion.createNotificationChannel(canal)
        }

        manejadorNotificacion.notify(0, constructorNotificacion.build())
    }
}