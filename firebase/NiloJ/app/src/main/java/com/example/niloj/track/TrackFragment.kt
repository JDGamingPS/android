package com.example.niloj.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.niloj.Constantes
import com.example.niloj.orden.OrderAux
import com.example.niloj.R
import com.example.niloj.databinding.FragmentTrackBinding
import com.example.niloj.entidades.Order
import com.google.firebase.firestore.FirebaseFirestore

class TrackFragment: Fragment() {

    private var binding: FragmentTrackBinding? = null

    private var orden: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackBinding.inflate(inflater, container, false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        obtenerOrden()
    }

    private fun obtenerOrden() {
        // aqui se actualiza la vista del fragmento
        orden = (activity as? OrderAux)?.obtenerOrdenSeleccioanda()

        orden?.let {
            actualiarUI(it)

            obtenerOrdenRealtime(it.id)

            configurarActionBar()
        }

    }

    private fun actualiarUI(orden: Order){
        binding?.let {
            it.progressBar.progress = orden.estado * (100/3) - 15

            it.cbOrder.isChecked = orden.estado > 0
            it.cbPreparando.isChecked = orden.estado > 1
            it.cbEnviando.isChecked = orden.estado > 2
            it.cbEntregado.isChecked = orden.estado > 3
        }
    }

    private fun obtenerOrdenRealtime(ordenId: String){
        val db = FirebaseFirestore.getInstance()

        val ordenRef = db.collection(Constantes.COLL_RESPUESTAS).document(ordenId)
        ordenRef.addSnapshotListener { snapshot, error ->
            if (error != null){
                Toast.makeText(activity, "Error al consultar esta orden", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()){
                val orden = snapshot.toObject(Order::class.java)
                orden?.let {
                    it.id = snapshot.id

                    actualiarUI(it)
                }
            }
        }
    }

    private fun configurarActionBar(){
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.track_title)
            setHasOptionsMenu(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            it.supportActionBar?.title = getString(R.string.order_title)
            setHasOptionsMenu(false)
        }

        super.onDestroy()
    }

}