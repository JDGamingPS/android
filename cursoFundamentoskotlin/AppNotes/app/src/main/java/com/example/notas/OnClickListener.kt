package com.example.notas

interface OnClickListener {
    fun onChecked(nota: Nota)
    fun onLongClick(nota: Nota, adaptadorActual: NotaAdapater)
}