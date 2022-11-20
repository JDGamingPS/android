package com.example.niloj.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.niloj.R
import com.example.niloj.databinding.ItemProductoBinding
import com.example.niloj.entidades.Producto

class ProductoAdaptador(private val productoList: MutableList<Producto>,
                        private val listener: OnProductoListener
) : RecyclerView.Adapter<ProductoAdaptador.ViewHolder>(){

    private lateinit var contexto: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val view = LayoutInflater.from(contexto).inflate(R.layout.item_producto, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productoList[position]

        holder.setListener(producto)

        holder.binding.tvNombre.text = producto.nombre
        holder.binding.tvPrecio.text = producto.precio.toString()
        holder.binding.tvCantidad.text = producto.cantidad.toString()

        Glide.with(contexto)
            .load(producto.imgUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_access_time) //-> por si hay algun error en cargar la imagen
            .error(R.drawable.ic_broken_image)
            .centerCrop()
            .into(holder.binding.imgProducto)
    }

    override fun getItemCount(): Int = productoList.size

    fun add(producto: Producto){
        if (!productoList.contains(producto)){
            productoList.add(producto)
            notifyItemInserted(productoList.size - 1)
        }else{
            actualizar(producto)
        }
    }

    fun actualizar(producto: Producto){
        val indice = productoList.indexOf(producto)
        if (indice != null){
            productoList.set(indice, producto)
            notifyItemChanged(indice)
        }
    }

    fun borrar(producto: Producto){
        val indice = productoList.indexOf(producto)
        if (indice != null){
            productoList.removeAt(indice)
            notifyItemRemoved(indice)
        }
    }

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemProductoBinding.bind(view)

        fun setListener(producto: Producto){
            binding.root.setOnClickListener {
                listener.onClick(producto)
            }

        }
    }

}