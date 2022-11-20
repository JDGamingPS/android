package com.example.nilopartnerj.chat

import com.example.nilopartnerj.entidades.Mensaje

interface OnChatListener {
    fun borrarMensaje(mensaje: Mensaje)
}