package com.example.niloj.Adaptadores

import com.example.niloj.entidades.Producto

interface MainAux {
    fun obtenerProductosCart(): MutableList<Producto>
    fun actualizarTotal()
    fun limpiarCarrito()

    fun obtenerProductoSeleccionado(): Producto?
    fun mostrarBotton(esVisible: Boolean)
    fun añadirAlCarrito(producto: Producto)
}