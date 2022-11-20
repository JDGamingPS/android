package com.example.niloj.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.niloj.Adaptadores.MainAux
import com.example.niloj.R
import com.example.niloj.databinding.FragmentDetalleBinding
import com.example.niloj.entidades.Producto

class DetalleFragment : Fragment(){

    private var binding: FragmentDetalleBinding? = null
    private var producto: Producto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDetalleBinding.inflate(inflater, container, false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        obtenerProducto()

        configurarBotones()

    }

    private fun obtenerProducto() {
        producto = (activity as? MainAux)?.obtenerProductoSeleccionado()
        producto?.let { producto->
            binding?.let {
                it.tvNombre.text = producto.nombre
                it.tvDescripcion.text = producto.descripcion
                it.tvCantidad.text = getString(R.string.detail_quantity, producto.cantidad)
                asignarNuevaCantidad(producto)

                Glide.with(this)
                    .load(producto.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(it.imgProducto)
            }
        }
    }

    private fun asignarNuevaCantidad(producto: Producto) {
        binding?.let {
            it.etNuevaCantidad.setText(producto.nuevaCantidad.toString())

            val nuevaCantidadSTR = getString(R.string.detail_total_price, producto.precioTotal(), producto.nuevaCantidad, producto.precio)
            it.tvtotalPrecio.text = HtmlCompat.fromHtml(nuevaCantidadSTR, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    // para asignar la nueva cantidad al carrito

    private fun configurarBotones(){
        producto?.let { producto ->
            binding?.let { binding ->
                binding.ibRest.setOnClickListener {
                    if (producto.nuevaCantidad > 1){
                        producto.nuevaCantidad -= 1
                        asignarNuevaCantidad(producto)
                    }
                }

                binding.ibSum.setOnClickListener {
                    if (producto.nuevaCantidad < producto.cantidad){
                        producto.nuevaCantidad += 1
                        asignarNuevaCantidad(producto)
                    }
                }

                binding.efab.setOnClickListener {
                    producto.nuevaCantidad = binding.etNuevaCantidad.text.toString().toInt()

                    addAlCarrito(producto)

                }
            }
        }
    }

    private fun addAlCarrito(producto: Producto){
        (activity as? MainAux)?.let {
            it.añadirAlCarrito(producto)
            activity?.onBackPressed()
        }
    }

    override fun onDestroy() {
        (activity as? MainAux)?.mostrarBotton(true)
        super.onDestroy()
        binding = null
    }
}