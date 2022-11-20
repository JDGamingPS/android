package com.example.niloj.cart

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.niloj.Adaptadores.MainAux
import com.example.niloj.Constantes
import com.example.niloj.orden.OrderActivity
import com.example.niloj.R
import com.example.niloj.databinding.FragmentCartBinding
import com.example.niloj.entidades.Order
import com.example.niloj.entidades.ProductOrder
import com.example.niloj.entidades.Producto
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : BottomSheetDialogFragment(), OnCartListener {

    private var binding: FragmentCartBinding? = null

    private lateinit var bottonSheetBehavior: BottomSheetBehavior<*>

    private lateinit var adaptadorPC: ProductoCartAdapter

    private var precioTotal = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentCartBinding.inflate(LayoutInflater.from(activity))
        binding?.let {
            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)

            bottonSheetBehavior = BottomSheetBehavior.from(it.root.parent as View)
            bottonSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            configararReciclerView()
            configurarBotones()

            obtenerProductos()

            return  bottomSheetDialog
        }

        return super.onCreateDialog(savedInstanceState)
    }

    private fun configararReciclerView(){
        binding?.let {
            adaptadorPC = ProductoCartAdapter(mutableListOf(), this)
            it.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@CartFragment.adaptadorPC // para asignar el adapadorPC al adaptador
            }

            /*(1..5).forEach {
                val producto = Producto(it.toString(), "producto $it", "este producto es $it", "", it, 2.0*it, )
                adapadorPC.add(producto)
            }*/
        }
    }

    private fun configurarBotones(){
        binding?.let {
            it.ibCancelar.setOnClickListener {
                bottonSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

            it.efab.setOnClickListener {
                requestOrder()
            }
        }
    }

    // se comunica con el main activity para traer los productos al cart fragment
    private fun obtenerProductos(){
        (activity as? MainAux)?.obtenerProductosCart()?.forEach {
            adaptadorPC.add(it)
        }
    }

    private fun requestOrder(){
        val usuario = FirebaseAuth.getInstance().currentUser
        usuario?.let { miUsuario ->

            habilitarUI(false)

            val productos = hashMapOf<String, ProductOrder>()
            adaptadorPC.obtenerProductos().forEach { producto ->
                //productos[producto.id!!, ] = ProductOrder(producto.id!!, producto.nombre!!, producto.nuevaCantidad)
                productos.put(producto.id!!, ProductOrder(producto.id!!, producto.nombre!!, producto.nuevaCantidad, producto.partnerId))
            }

            val orden = Order(clienteId = miUsuario.uid, productos = productos, precioTotal = precioTotal, estado = 1)

            val db = FirebaseFirestore.getInstance()
            db.collection(Constantes.COLL_RESPUESTAS)
                .add(orden)
                .addOnSuccessListener {
                    dismiss()
                    (activity as? MainAux)?.limpiarCarrito()
                    startActivity(Intent(context, OrderActivity::class.java))

                    Toast.makeText(activity, "Compra realizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al comprar", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    habilitarUI(true)
                }
        }

    }

    private fun habilitarUI(habilitado: Boolean){
        binding?.let {
            it.ibCancelar.isEnabled = habilitado
            it.efab.isEnabled = habilitado
        }
    }

    override fun onDestroyView() {
        (activity as? MainAux)?.actualizarTotal()
        super.onDestroyView()
        binding = null
    }

    override fun asignarCantidad(producto: Producto) {
        adaptadorPC.actualizar(producto)
    }

    override fun mostratTotal(total: Double) {
        precioTotal = total
        binding?.let {
            it.tvTotal.text = getString(R.string.product_full_cart, total)
        }

    }
}