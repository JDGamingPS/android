package com.example.nilopartnerj.order

import com.example.nilopartnerj.entidades.Order

interface OnOrderListener {
    fun onStartChat(order: Order)
    fun onCambioEstado(ordem: Order)
}