package com.example.niloj.chat

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.niloj.R
import com.example.niloj.databinding.ItemChatBinding
import com.example.niloj.entidades.Mensaje

class ChatAdapter(private val mensajeList: MutableList<Mensaje>, val listener: OnChatListener) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensaje = mensajeList[position]
        holder.setListener(mensaje)

        var gravity = Gravity.END
        var background = ContextCompat.getDrawable(context, R.drawable.background_chat_client)
        var textColor = ContextCompat.getColor(context, R.color.colorOnSecondary)

        val marginHorizontal = context.resources.getDimensionPixelSize(R.dimen.chat_margin_horizontal)
        val params = holder.binding.tvMensaje.layoutParams as ViewGroup.MarginLayoutParams // -> estamos extraendo los paremetros actuales
        params.marginStart = marginHorizontal
        params.marginEnd = 0
        params.topMargin = 0

        if (position > 0 && mensaje.esEnviadoPorMi() != mensajeList[position - 1 ].esEnviadoPorMi()){
            params.topMargin = context.resources.getDimensionPixelSize(R.dimen.common_padding_min)
        }

        if (!mensaje.esEnviadoPorMi()){
            //mensaje de parte del vendedor
            gravity = Gravity.START
            background = ContextCompat.getDrawable(context, R.drawable.background_chat_support)
            textColor = ContextCompat.getColor(context, R.color.colorOnSecondary)
            params.marginStart = 0
            params.marginEnd = marginHorizontal
        }

        holder.binding.root.gravity = gravity

        holder.binding.tvMensaje.layoutParams = params
        holder.binding.tvMensaje.background = background
        holder.binding.tvMensaje.setTextColor(textColor)
        holder.binding.tvMensaje.text = mensaje.mensaje

    }

    override fun getItemCount(): Int {
        return mensajeList.size
    }

    fun add(mensaje: Mensaje){
        if (!mensajeList.contains(mensaje)){
            mensajeList.add(mensaje)
            notifyItemInserted(mensajeList.size-1)
        }
    }

    fun actualizar(mensaje: Mensaje){
        val indice = mensajeList.indexOf(mensaje)
        if (indice != -1){
            mensajeList[indice] = mensaje
            notifyItemChanged(indice)
        }
    }

    fun borrar(mensaje: Mensaje){
        val indice = mensajeList.indexOf(mensaje)
        if (indice != -1){
            mensajeList.removeAt(indice)
            notifyItemRemoved(indice)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemChatBinding.bind(view)

        fun setListener(mensaje: Mensaje){
            binding.tvMensaje.setOnLongClickListener {
                listener.borrarMensaje(mensaje)
                true
            }
        }
    }
}