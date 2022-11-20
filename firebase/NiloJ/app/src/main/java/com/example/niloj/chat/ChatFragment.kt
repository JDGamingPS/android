package com.example.niloj.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.niloj.Constantes
import com.example.niloj.R
import com.example.niloj.databinding.FragmentChatBinding
import com.example.niloj.entidades.Mensaje
import com.example.niloj.entidades.Order
import com.example.niloj.orden.OrderAux
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment: Fragment(), OnChatListener {

    private var binding: FragmentChatBinding? = null

    private lateinit var adapterChat: ChatAdapter

    private var orden: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        obtenerOrden()

        configurarReciclerViewChat()

        configaracionBotones()
    }

    private fun obtenerOrden(){
        orden = (activity as? OrderAux)?.obtenerOrdenSeleccioanda()
        orden?.let {
            configurarActionBar()
            configurarRealtimeDataBase()
        }
    }

    private fun configurarRealtimeDataBase() {
        orden?.let {
            val db = Firebase.database
            val chatRef = db.getReference(Constantes.RUTA_CHATS).child(it.id)
            val childListener = object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    /*val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let { mensaje->
                        // es la llave que se tiene de cada mensaje enviado en el realtime database
                        snapshot.key?.let {
                            mensaje.id= it
                        }

                        FirebaseAuth.getInstance().currentUser?.let { usuario->
                            mensaje.myUId = usuario.uid
                        }
                        adapterChat.add(mensaje)
                        binding?.recyclerViewChat?.scrollToPosition(adapterChat.itemCount - 1) // -> para empujar los chats hacia arriba osea que se vea el yltimo emnsaje enviado
                    }*/
                    obtenerMensaje(snapshot)?.let {
                        adapterChat.add(it)
                        binding?.recyclerViewChat?.scrollToPosition(adapterChat.itemCount - 1) // -> para empujar los chats hacia arriba osea que se vea el yltimo emnsaje enviado
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    /*val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let { mensaje->
                        // es la llave que se tiene de cada mensaje enviado en el realtime database
                        snapshot.key?.let {
                            mensaje.id= it
                        }

                        FirebaseAuth.getInstance().currentUser?.let { usuario->
                            mensaje.myUId = usuario.uid
                        }
                        adapterChat.actualizar(mensaje)
                    }*/
                    obtenerMensaje(snapshot)?.let {
                        adapterChat.actualizar(it)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    /*val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let { mensaje->
                        // es la llave que se tiene de cada mensaje enviado en el realtime database
                        snapshot.key?.let {
                            mensaje.id= it
                        }

                        FirebaseAuth.getInstance().currentUser?.let { usuario->
                            mensaje.myUId = usuario.uid
                        }
                        adapterChat.borrar(mensaje)
                    }*/
                    obtenerMensaje(snapshot)?.let {
                        adapterChat.borrar(it)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                    binding?.let { binding ->
                        Snackbar.make(binding.root, "Error al cargar el chat", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            chatRef.addChildEventListener(childListener)
        }
    }

    private fun obtenerMensaje(snapshot: DataSnapshot): Mensaje? {
        //val mensaje = snapshot.getValue(Mensaje::class.java)
        snapshot.getValue(Mensaje::class.java)?.let { mensaje->
            // es la llave que se tiene de cada mensaje enviado en el realtime database
            snapshot.key?.let {
                mensaje.id= it
            }
            FirebaseAuth.getInstance().currentUser?.let { usuario->
                mensaje.myUId = usuario.uid
            }
            return mensaje
        }
        return null
    }

    private fun configurarReciclerViewChat(){
        adapterChat = ChatAdapter(mutableListOf(), this)
        binding?.let {
            it.recyclerViewChat.apply {
                layoutManager = LinearLayoutManager(context).also {
                    it.stackFromEnd = true
                }
                adapter = this@ChatFragment.adapterChat
            }
        }

        /*(1..20).forEach {
            adapterChat.add(Mensaje(it.toString(), if (it%4 == 0) "hola, como estas?, hola, como estas?, hola, como estas?" else "hola, como estas?",
                if (it%3 == 0) "tu" else "yo", "yo"))
        }*/
    }

    private fun configaracionBotones()
    {
        binding?.let { binding->
            binding.ibEnviar.setOnClickListener {
                enviarMensaje()
            }
        }
    }

    private fun enviarMensaje(){

        binding?.let { binding ->
            orden?.let {
                val db = Firebase.database
                val chatRef = db.getReference(Constantes.RUTA_CHATS).child(it.id)
                val usuario = FirebaseAuth.getInstance().currentUser
                usuario?.let {
                    val mensaje = Mensaje(mensaje = binding.etMensaje.text.toString().trim(), remitente = it.uid)

                    binding.ibEnviar.isEnabled = false

                    chatRef.push().setValue(mensaje)
                        .addOnSuccessListener {
                            binding.etMensaje.setText("")
                        }
                        .addOnCompleteListener {
                            binding.ibEnviar.isEnabled = true
                        }
                }
            }
        }
    }

    private fun configurarActionBar(){
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.chat_title)
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

    override fun borrarMensaje(mensaje: Mensaje) {
        orden?.let {
            val dataBase = Firebase.database
            val mensajeRef = dataBase.getReference(Constantes.RUTA_CHATS).child(it.id).child(mensaje.id)//para ubicar el utlimo de la rama de reltime
            mensajeRef.removeValue { error, ref ->
                binding?.let { binding ->
                    if (error != null){
                        Snackbar.make(binding.root, "Error al cargar el chat", Snackbar.LENGTH_LONG).show()
                    }else{
                        Snackbar.make(binding.root, "Mensaje Borrado", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

    }
}