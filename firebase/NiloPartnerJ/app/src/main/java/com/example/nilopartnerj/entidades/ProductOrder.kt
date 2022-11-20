package com.example.nilopartnerj.entidades

import com.google.firebase.firestore.Exclude

data class ProductOrder(@get: Exclude var id: String = "",
                        var nombre: String = "",
                        var cantidad: Int = 0,
                        var partnerId: String = ""){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductOrder

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
