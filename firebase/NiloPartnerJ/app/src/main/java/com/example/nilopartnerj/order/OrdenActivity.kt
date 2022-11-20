package com.example.nilopartnerj.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilopartnerj.Constantes
import com.example.nilopartnerj.R
import com.example.nilopartnerj.chat.ChatFragment
import com.example.nilopartnerj.databinding.ActivityOrdenBinding
import com.example.nilopartnerj.entidades.Order
import com.example.nilopartnerj.fcm.NotificacionServicioRemoto
import com.google.firebase.firestore.FirebaseFirestore

class OrdenActivity : AppCompatActivity(), OnOrderListener, OrderAux {

    private lateinit var binding: ActivityOrdenBinding

    private lateinit var adaptadorOrd: OrderAdapter

    private lateinit var ordernSeleccionada: Order

    private val aValores: Array<String> by lazy {
        resources.getStringArray(R.array.estado_valores)
    }

    private val aLlaves: Array<Int> by lazy {
        resources.getIntArray(R.array.estado_llave).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarFireStore()
    }

    private fun configurarRecyclerView() {
        adaptadorOrd = OrderAdapter(mutableListOf(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrdenActivity)
            adapter = this@OrdenActivity.adaptadorOrd // -> adapter es propio del recycler
        }


    }

    private fun configurarFireStore(){
        val db = FirebaseFirestore.getInstance()
        db.collection(Constantes.COLL_RESPUESTAS).get()
            .addOnSuccessListener {
                for (documento in it){
                    val orden = documento.toObject(Order::class.java)
                    orden.id = documento.id
                    adaptadorOrd.add(orden)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Consultar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun nofocacionCliente(orden: Order){
        val db = FirebaseFirestore.getInstance()

        db.collection(Constantes.COLL_USERS)
            .document(orden.clienteId)
            .collection(Constantes.COLL_TOKENS)
            .get()
            .addOnSuccessListener {
                var tokensSTR = ""
                for (documento in it){
                    val tokenMap = documento.data
                    tokensSTR += "${tokenMap.getValue(Constantes.PROP_TOKEN)},"
                }

                if (tokensSTR.length > 0){
                    tokensSTR = tokensSTR.dropLast(1)

                    var nombres =""
                    orden.productos.forEach{
                        nombres += "${it.value.nombre}, "
                    }
                    nombres = nombres.dropLast(2)

                    val indice = aLlaves.indexOf(orden.estado)

                    val notificacionRS = NotificacionServicioRemoto()
                    notificacionRS.enviarNotificacion("tu mensaje a sido ${aValores[indice]}", nombres, tokensSTR)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al Consultar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStartChat(order: Order) {
        ordernSeleccionada = order

        val fragmentChat = ChatFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.contenedorMainOrder, fragmentChat)
            .addToBackStack(null)
            .commit()
    }

    override fun onCambioEstado(orden: Order) {
        val db = FirebaseFirestore.getInstance()
        db.collection(Constantes.COLL_RESPUESTAS)
            .document(orden.id)
            .update(Constantes.RUTA_ESTADO, orden.estado)
            .addOnSuccessListener {
                Toast.makeText(this, "Orden Actualizada", Toast.LENGTH_SHORT).show()
                nofocacionCliente(orden)
            }
            .addOnFailureListener {

                Toast.makeText(this, "Error al actualizar orden", Toast.LENGTH_SHORT).show()
            }
    }

    override fun obtenerOrdenSeleccioanda(): Order {
        return ordernSeleccionada
    }

}