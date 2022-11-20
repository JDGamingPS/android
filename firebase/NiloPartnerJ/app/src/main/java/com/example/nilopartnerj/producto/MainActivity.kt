package com.example.nilopartnerj.producto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nilopartnerj.adicionar.AddDialogFragment
import com.example.nilopartnerj.Constantes
import com.example.nilopartnerj.entidades.Producto
import com.example.nilopartnerj.R
import com.example.nilopartnerj.databinding.ActivityMainBinding
import com.example.nilopartnerj.order.OrdenActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity(), OnProductoListener, MainAux {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var estadoDeAutenticacion: FirebaseAuth.AuthStateListener

    private lateinit var adaptador: ProductoAdaptador

    private lateinit var firestoreListener: ListenerRegistration

    private var productoSeleccionado: Producto?=null

    // para iniciar session
    private val lanzadorResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val respuesta = IdpResponse.fromResultIntent(it.data)

            if (it.resultCode == RESULT_OK){
                val usuario = FirebaseAuth.getInstance().currentUser
                if (usuario != null){
                    Toast.makeText(this,  "Bienvenido", Toast.LENGTH_SHORT).show()
                }
            }else{
                if (respuesta == null){
                    Toast.makeText(this,  "Hasta Pronto", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    respuesta.error?.let {
                        if (it.errorCode == ErrorCodes.NO_NETWORK){
                            Toast.makeText(this,  "Sin Red", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this,  "Codigo de error: ${it.errorCode}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        //configFireStore()
        //configFireStoreRealTime()
        configButtons()


    }

    private fun configAuth(){

        // sesion activa
        firebaseAuth = FirebaseAuth.getInstance()
        estadoDeAutenticacion = FirebaseAuth.AuthStateListener { autenticación->
            if (autenticación.currentUser != null){
                supportActionBar?.title = autenticación.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.nsvProductos.visibility = View.VISIBLE
                binding.efab.show()
            }else{
                // iniciar sesion
                val proveedores = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())
                lanzadorResultado.launch(AuthUI
                    .getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(proveedores)
                    .setIsSmartLockEnabled(false)
                    .build())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // para verificar si ya hay una sesion iniciada
        firebaseAuth.addAuthStateListener(estadoDeAutenticacion)
        //para llamar el FireStore
        configFireStoreRealTime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(estadoDeAutenticacion)
        // para eliminar el Firestore
        firestoreListener.remove()
    }

    private fun configRecyclerView(){
        adaptador = ProductoAdaptador(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3, GridLayoutManager.HORIZONTAL, false)
            adapter = this@MainActivity.adaptador
        }

        /*(1..28).forEach{
            val producto = Producto(it.toString(), "producto $it", descripcion = "este es el producto $it",
                "", it, it*1.1)

            adaptador.add(producto)
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // para inflar el menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            // para cerrar sesion
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this,  "Sesion terminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.nsvProductos.visibility = View.GONE
                            //mostrar el progressbar al cerrar la sesion
                            binding.llProgress.visibility = View.VISIBLE
                            binding.efab.hide()
                        }else{
                            Toast.makeText(this,  "No se pudo cerrar la sesion", Toast.LENGTH_SHORT).show()
                        }
                    }

            }
            R.id.accion_orden_historial ->{
                startActivity(Intent(this, OrdenActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configFireStore(){
        val db = FirebaseFirestore.getInstance() // acceso a la base de datos de FireStore
        db.collection(Constantes.COLL_PRODUCTOS)
            .get()
            .addOnSuccessListener { snapshots ->
                for (documento in snapshots){
                    val producto = documento.toObject(Producto::class.java)
                    // para que podamos visializar los productos en el dispositivo asignamos el id del documento al producto para visualizarlo
                    producto.id = documento.id
                    adaptador.add(producto)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,  "Error al consultar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configFireStoreRealTime(){
        val db = FirebaseFirestore.getInstance()
        val refProducto = db.collection(Constantes.COLL_PRODUCTOS)

        firestoreListener = refProducto.addSnapshotListener{snapshots, error ->
            if (error != null){
                Toast.makeText(this,  "Error al consultar datos", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges){
                val producto = snapshot.document.toObject(Producto::class.java)
                producto.id = snapshot.document.id
                when(snapshot.type){
                    //propios de fireStore
                    ADDED -> adaptador.add(producto)
                    MODIFIED -> adaptador.actualizar(producto)
                    REMOVED -> adaptador.borrar(producto)
                }
            }
        }

    }

    private fun configButtons(){
        binding.efab.setOnClickListener {
            productoSeleccionado = null
            AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
        }
    }

    override fun onClick(producto: Producto) {
        productoSeleccionado = producto
        AddDialogFragment().show(supportFragmentManager, AddDialogFragment::class.java.simpleName)
    }

    override fun onLongClick(producto: Producto) {
        val db = FirebaseFirestore.getInstance()
        val refProducto = db.collection(Constantes.COLL_PRODUCTOS)
        producto.id?.let {  id ->
            refProducto.document(id)
                .delete()
                .addOnFailureListener {
                    Toast.makeText(this,  "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun obtenerProductoSeleccionado(): Producto? = productoSeleccionado
}