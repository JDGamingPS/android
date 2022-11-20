package com.example.niloj.orden

import com.example.niloj.entidades.Order

interface OnOrderListener {
    fun onTrack(order: Order)
    fun onStartChat(order: Order)

}