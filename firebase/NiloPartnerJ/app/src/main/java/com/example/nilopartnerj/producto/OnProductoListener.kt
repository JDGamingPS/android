package com.example.nilopartnerj.producto

import com.example.nilopartnerj.entidades.Producto

interface OnProductoListener {
    fun onClick(producto: Producto)
    fun onLongClick(producto: Producto)
}