package com.example.niloj.Adaptadores

import com.example.niloj.entidades.Producto

interface OnProductoListener {
    fun onClick(producto: Producto)
    //fun onLongClick(producto: Producto) -> no usaremos este metodo ya no eliminaremos un prducto solo estamis en modo lectura
}