package com.example.nilopartnerj.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nilopartnerj.R
import com.example.nilopartnerj.databinding.ItemOrderBinding
import com.example.nilopartnerj.entidades.Order

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

        holder.binding.tvPrecioTotal.text = context.getString(R.string.order_total_price, order.precioTotal )


        val indice = aLlaves.indexOf(order.estado)

        // alimentar el spinner con los estados
        val estadoAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, aValores)

        holder.binding.actvEstado.setAdapter(estadoAdapter)
        if (indice != -1){
            holder.binding.actvEstado.setText(aValores[indice], false)
        }
        else{
            holder.binding.actvEstado.setText(context.getString(R.string.order_status_unknown), false)//-> false para que no sea editable el spinner
        }

    }

    override fun getItemCount(): Int = orderList.size

    fun add(order: Order){
        orderList.add(order)
        notifyItemInserted(orderList.size-1)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val binding = ItemOrderBinding.bind(view)

        fun setListener(order: Order){
            binding.actvEstado.setOnItemClickListener { adapterView, view, posicion, id ->
                order.estado = aLlaves[posicion]
                listener.onCambioEstado(order)
            }

            binding.chipChat.setOnClickListener {
                listener.onStartChat(order)
            }
        }
    }

}