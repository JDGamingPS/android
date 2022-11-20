package com.example.niloj.entidades

import com.google.firebase.database.Exclude //-> database realtimeDatabase


data class Mensaje(@get:Exclude var id: String = "",
                   var mensaje: String= "",
                   var remitente: String="", //-> remitente, remite o envia algo
                   @get:Exclude
                   var myUId: String =""){

    @Exclude // -A para que no se registe el remitente
    fun esEnviadoPorMi(): Boolean = remitente.equals(myUId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mensaje

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
