package com.example.niloj

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.niloj.Adaptadores.MainAux
import com.example.niloj.Adaptadores.OnProductoListener
import com.example.niloj.Adaptadores.ProductoAdaptador
import com.example.niloj.orden.OrderActivity
import com.example.niloj.cart.CartFragment
import com.example.niloj.databinding.ActivityMainBinding
import com.example.niloj.detalle.DetalleFragment
import com.example.niloj.entidades.Producto
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), OnProductoListener, MainAux {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var estadoDeAutenticacion: FirebaseAuth.AuthStateListener

    private lateinit var adaptador: ProductoAdaptador

    private lateinit var firestoreListener: ListenerRegistration

    private var productoSeleccioando: Producto? = null

    private val productoCartList = mutableListOf<Producto>()

    // para iniciar session
    private val lanzadorResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val respuesta = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){
            val usuario = FirebaseAuth.getInstance().currentUser
            if (usuario != null){
                Toast.makeText(this,  "Bienvenido", Toast.LENGTH_SHORT).show()

                // es justo aqui que registraremos el tokken del usuario autenticado
                val preferencias = PreferenceManager.getDefaultSharedPreferences(this)
                val token = preferencias.getString(Constantes.PROP_TOKEN, null)

                token?.let {
                    val db = FirebaseFirestore.getInstance()
                    val tokenMap = hashMapOf(Pair(Constantes.PROP_TOKEN, token))

                    db.collection(Constantes.COLL_USERS)
                        .document(usuario.uid) //-> se crea una documento llamada usuarios en firebase
                        .collection(Constantes.COLL_TOKENS)// -> se crea una coleccion llamada tokkens
                        .add(tokenMap)// añade un acolecion de tokens
                        .addOnSuccessListener {
                            Log.i("token registrado", token)
                            preferencias.edit {
                                putString(Constantes.PROP_TOKEN, null).apply() //-> para limpiar las preferencias

                            }
                        }
                        .addOnFailureListener {
                            Log.i("token no registrado", token)
                        }
                }
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
        configButtons()

        // obtener tokken de forma manual FCM
        /*FirebaseMessaging.getInstance().token.addOnCompleteListener { tarea ->
            if (tarea.isSuccessful){
                val token =  tarea.result
                Log.i("obtener token", token.toString())
            }else{
                Log.i("get token fail", tarea.exception.toString())
            }
        }*/
    }

    private fun configAuth(){

        // sesion activa
        firebaseAuth = FirebaseAuth.getInstance()
        estadoDeAutenticacion = FirebaseAuth.AuthStateListener { autenticación->
            if (autenticación.currentUser != null){
                supportActionBar?.title = autenticación.currentUser?.displayName
                binding.llProgress.visibility = View.GONE
                binding.nsvProductos.visibility = View.VISIBLE
            }else{
                // iniciar sesion
                val proveedores = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())
                lanzadorResultado.launch(
                    AuthUI
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
    }

    private fun configButtons(){
        binding.btnViewCard.setOnClickListener {
            val fragmento = CartFragment()
            fragmento.show(supportFragmentManager.beginTransaction(), CartFragment::class.java.simpleName)
        }
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
                        }else{
                            Toast.makeText(this,  "No se pudo cerrar la sesion", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            R.id.accion_orden_historial ->{
                startActivity(Intent(this, OrderActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // FireStore
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
                    DocumentChange.Type.ADDED -> adaptador.add(producto)
                    DocumentChange.Type.MODIFIED -> adaptador.actualizar(producto)
                    DocumentChange.Type.REMOVED -> adaptador.borrar(producto)
                }
            }
        }

    }

    override fun onClick(producto: Producto) {
        val indice = productoCartList.indexOf(producto)
        if (indice != -1){ // evitar producto repetido en el arreglo que es error interno
            productoSeleccioando = productoCartList[indice]
        } else {
            productoSeleccioando = producto
        }

        val fragment = DetalleFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.contenedorMain, fragment)
            .addToBackStack(null)
            .commit()

        mostrarBotton(false)
    }

    override fun obtenerProductosCart(): MutableList<Producto> {

        /*(1..7).forEach {
                val producto = Producto(it.toString(), "producto $it", "este producto es $it", "", it, precio = 2.0*it, )
                productoCartList.add(producto)
            }*/
        return productoCartList
    }

    override fun obtenerProductoSeleccionado(): Producto? = productoSeleccioando

    override fun mostrarBotton(esVisible: Boolean) {
        binding.btnViewCard.visibility =
            if (esVisible)
                View.VISIBLE
            else
                View.GONE
    }

    override fun añadirAlCarrito(producto: Producto) {

        // evitar producto repetido en el arreglo: productoCartList del adaptador error interno
        val indice = productoCartList.indexOf(producto)
        if (indice != -1){
            productoCartList[indice] = producto
        } else {
            productoCartList.add(producto)
        }

        actualizarTotal()
    }

    override fun actualizarTotal() {
        var total = 0.0
        productoCartList.forEach {  producto ->
            total += producto.precioTotal()
        }

        if (total == 0.0){
            binding.tvTotal.text = getString(R.string.product_empty_cart)
        }else{
            binding.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }

    override fun limpiarCarrito() {
        productoCartList.clear()
    }
}