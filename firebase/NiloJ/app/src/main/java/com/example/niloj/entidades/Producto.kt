package com.example.niloj.entidades

import com.google.firebase.firestore.Exclude

data class Producto(@get:Exclude var id: String? = null,
                    var nombre: String?= null,
                    var descripcion: String?= null,
                    var imgUrl: String?= null,
                    var cantidad: Int = 0,
                    @get:Exclude var nuevaCantidad: Int = 1,
                    var precio : Double = 0.0,
                    var partnerId:String = ""){

    fun precioTotal(): Double = nuevaCantidad * precio

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
