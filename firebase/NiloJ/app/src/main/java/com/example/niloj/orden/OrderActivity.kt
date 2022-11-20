package com.example.niloj.orden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.niloj.Constantes
import com.example.niloj.R
import com.example.niloj.chat.ChatFragment
import com.example.niloj.databinding.ActivityOrderBinding
import com.example.niloj.entidades.Order
import com.example.niloj.track.TrackFragment
import com.google.firebase.firestore.FirebaseFirestore

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux{

    private lateinit var binding: ActivityOrderBinding

    private lateinit var adaptadorOrd: OrderAdapter

    private lateinit var ordernSeleccionada: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarFireStore()
    }

    private fun configurarRecyclerView() {
        adaptadorOrd = OrderAdapter(mutableListOf(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = this@OrderActivity.adaptadorOrd // -> adapter es propio del recycler
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

    override fun onTrack(order: Order) {
        ordernSeleccionada = order
        //lanzar el fragmento de trackFragement
        val fragmentoTrk = TrackFragment()
        supportFragmentManager.beginTransaction()
                //cambiar esto si salta un errror de contenedorMainOrder "a" contenedorMain
                //para lanzar un fragmento debe estar en la misma activity y llamarlo con el id de la actividad
            .add(R.id.contenedorMainOrder, fragmentoTrk)
            .addToBackStack(null)
            .commit()
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

    override fun obtenerOrdenSeleccioanda(): Order {
        return ordernSeleccionada
    }
}