package com.example.niloj.cart

import com.example.niloj.entidades.Producto

interface OnCartListener {
    fun asignarCantidad(producto: Producto)
    fun mostratTotal(total: Double)
}