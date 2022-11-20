package com.example.niloj.orden

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.niloj.R
import com.example.niloj.databinding.ItemOrderBinding
import com.example.niloj.entidades.Order

class OrderAdapter(private val orderList: MutableList<Order>, private val listener: OnOrderListener):
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private lateinit var context: Context

    private val aValores: Array<String> by lazy {
        context.resources.getStringArray(R.array.estado_valores)
    }

    private val aLlaves: Array<Int> by lazy {
        context.resources.getIntArray(R.array.estado_llave).toTypedArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.setListener(order)

        holder.binding.tvId.text = context.getString(R.string.order_id, order.id)

        //concatenar los nombres de las ordenes
        var nombres = ""
        order.productos.forEach {
            nombres += "${it.value.nombre}, "
        }
        holder.binding.tvNombresProductos.text = nombres.dropLast(2)

        holder.binding.tvPrecioTotal.text = context.getString(R.string.product_full_cart, order.precioTotal )


        val indice = aLlaves.indexOf(order.estado)
        val estadoStr = if (indice != -1) {
            aValores[indice]
        } else {
            context.getString(R.string.order_status_unknown)
        }
        holder.binding.tvEstado.text = context.getString(R.string.order_status, estadoStr)
    }

    override fun getItemCount(): Int = orderList.size

    fun add(order: Order){
        orderList.add(order)
        notifyItemInserted(orderList.size-1)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val binding = ItemOrderBinding.bind(view)

        fun setListener(order: Order){
            binding.btnTrack.setOnClickListener {
                listener.onTrack(order)
            }

            binding.chipChat.setOnClickListener {
                listener.onStartChat(order)
            }
        }
    }

}