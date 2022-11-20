package com.example.niloj.chat

import com.example.niloj.entidades.Mensaje

interface OnChatListener {
    fun borrarMensaje(mensaje: Mensaje)
}