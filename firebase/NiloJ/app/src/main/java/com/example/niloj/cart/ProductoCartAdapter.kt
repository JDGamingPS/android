package com.example.niloj.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.niloj.R
import com.example.niloj.databinding.ItemProductoCartBinding
import com.example.niloj.entidades.Producto

// recicler view de fragment_cart

class ProductoCartAdapter(private val productoList: MutableList<Producto>,
                          private val listener: OnCartListener): RecyclerView.Adapter<ProductoCartAdapter.ViewHolder>() {

    private lateinit var contexto: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val view = LayoutInflater.from(contexto).inflate(R.layout.item_producto_cart, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productoList[position]

        holder.setListener(producto)

        holder.binding.tvNombre.text = producto.nombre
        holder.binding.tvCantidad.text = producto.nuevaCantidad.toString()

        Glide.with(contexto)
            .load(producto.imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_access_time) //-> por si hay algun error en cargar la imagen
            .error(R.drawable.ic_broken_image)
            .centerCrop()
            .circleCrop()
            .into(holder.binding.imgProducto)
    }

    override fun getItemCount(): Int = productoList.size

    fun add(producto: Producto){
        if (!productoList.contains(producto)){
            productoList.add(producto)
            notifyItemInserted(productoList.size - 1)
            calcularTotal()
        }else{
            actualizar(producto)
        }
    }

    // para que el carrito sea dinamico
    fun actualizar(producto: Producto){
        val indice = productoList.indexOf(producto)
        if (indice != null){
            productoList.set(indice, producto)
            notifyItemChanged(indice)
            calcularTotal()
        }
    }

    fun borrar(producto: Producto){
        val indice = productoList.indexOf(producto)
        if (indice != null){
            productoList.removeAt(indice)
            notifyItemRemoved(indice)
            calcularTotal()
        }
    }

    //esta funcion lo iniciamod despues de las notificaciones de add, borrar, actualizar
    private fun calcularTotal(){
        var result = 0.0
        for (producto in productoList){
            result += producto.precioTotal()
        }

        listener.mostratTotal(result)
    }

    fun obtenerProductos(): List<Producto> = productoList


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val binding = ItemProductoCartBinding.bind(view)

        fun setListener(producto: Producto){
            binding.ibSum.setOnClickListener {
                if (producto.nuevaCantidad < producto.cantidad){
                    producto.nuevaCantidad += 1
                    listener.asignarCantidad(producto)
                }


            }
            binding.ibRestar.setOnClickListener {
                if (producto.nuevaCantidad > 0){
                    producto.nuevaCantidad -= 1
                    listener.asignarCantidad(producto)
                }

            }
        }

    }

}