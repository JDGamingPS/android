package com.example.nilopartnerj.adicionar

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilopartnerj.Constantes
import com.example.nilopartnerj.entidades.EventPost
import com.example.nilopartnerj.entidades.Producto
import com.example.nilopartnerj.producto.MainAux
import com.example.nilopartnerj.databinding.FragmentDialogAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddDialogFragment : DialogFragment(), DialogInterface.OnShowListener{

    private var binding: FragmentDialogAddBinding? = null

    private var botonPositivo: Button?= null
    private var botonNegativo: Button?= null

    private var producto: Producto? = null

    private var fotoSeleccionadaUri: Uri? = null

    //registro de la actividad para cargar una imgane
    private val lanzadorResultado = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            fotoSeleccionadaUri = it.data?.data

            //binding?.imgProductoPreview?.setImageURI(fotoSeleccionadaUri)
            binding?.let {
                Glide.with(this)
                    .load(fotoSeleccionadaUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgProductoPreview)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //si la actividad es distinto de null hacer
        activity?.let { activity ->
            //inflamos el binding Dialog
            binding = FragmentDialogAddBinding.inflate(LayoutInflater.from(context))

            binding?.let {
                val  constructor = AlertDialog.Builder(activity)
                    .setTitle("Agregar Producto")
                    .setPositiveButton("Agregar", null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)

                val dialog = constructor.create()
                dialog.setOnShowListener(this)

                return dialog
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialogInterface: DialogInterface) {
        //inicializar el prodcuto
        initProducto()

        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            botonPositivo = it.getButton(Dialog.BUTTON_POSITIVE)
            botonNegativo = it.getButton(Dialog.BUTTON_NEGATIVE)

            botonPositivo?.setOnClickListener {
                binding?.let {
                    habilitarUI(false)

                    subirImagen(producto?.id){ eventPost ->
                        if (eventPost.esCorrecto){
                            if (producto == null){
                                //para crear un nuevo producto
                                val productoN = Producto(nombre = it.etNombre.text.toString().trim(),
                                    descripcion = it.etDescripcion.text.toString().trim(),
                                    imgUrl = eventPost.fotoUrl,
                                    cantidad = it.etCantidad.text.toString().toInt(),
                                    precio = it.etPrecio.text.toString().toDouble(),
                                    partnerId = FirebaseAuth.getInstance().uid!!)

                                guardar(productoN, eventPost.documentoId!!)
                            }else{
                                //actualizar in producto
                                producto?.apply {
                                    nombre = it.etNombre.text.toString().trim()
                                    descripcion = it.etDescripcion.text.toString().trim()
                                    imgUrl = eventPost.fotoUrl
                                    cantidad = it.etCantidad.text.toString().toInt()
                                    precio = it.etPrecio.text.toString().toDouble()

                                    actualizar(this)
                                }
                            }
                        }
                    }



                }
                /*val producto = Producto(nombre = binding?.etNombre.text.toString().trim(),
                descripcion = binding?.etDescripcion?.text.toString().trim())*/

            }

            botonNegativo?.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun initProducto(){
        producto = (activity as? MainAux)?.obtenerProductoSeleccionado()
        producto?.let { producto ->
            binding?.let {
                it.etNombre.setText(producto.nombre)
                it.etDescripcion.setText(producto.descripcion)
                it.etCantidad.setText(producto.cantidad.toString())
                it.etPrecio.setText(producto.precio.toString())

                Glide.with(this)
                    .load(producto.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgProductoPreview)
            }
        }
    }

    private fun configButtons(){
        binding?.let {
            it.ibProducto.setOnClickListener {
                abrirGaleria()
            }
        }
    }

    private fun abrirGaleria(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        lanzadorResultado.launch(intent)
    }

    private fun subirImagen(productoId: String?, callback: (EventPost)->Unit){
        val eventPost = EventPost()
        // guarda el id del producto antes de subirlo
        eventPost.documentoId = productoId ?:  FirebaseFirestore.getInstance().collection(Constantes.COLL_PRODUCTOS).document().id

        val storageRef = FirebaseStorage.getInstance().reference.child(Constantes.PATH_PRODUCT_IMGES)

        fotoSeleccionadaUri?.let { uri->
            binding?.let { binding->
                binding.progressBar.visibility = View.VISIBLE
                // asigna el id a la ruta del storage del producto que se guardo
                val fotoRef = storageRef.child(eventPost.documentoId!!)

                fotoRef.putFile(uri)
                    .addOnProgressListener {
                        val progress = (100*it.bytesTransferred / it.totalByteCount).toInt()
                        it.run {
                            binding.progressBar.progress = progress
                            binding.tvProgress.text = String.format("%s%%", progress)
                        }
                    }
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { downloadURL ->
                            Log.i("URL", downloadURL.toString())
                            eventPost.esCorrecto = true
                            eventPost.fotoUrl = downloadURL.toString()
                            callback(eventPost)
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(activity, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        eventPost.esCorrecto = false

                        habilitarUI(true)
                        callback(eventPost)
                    }
            }
        }
    }
    // aqui va con otro id al subir un producto nuevo

    private fun guardar(producto: Producto, documentId: String){
        //Instanciamos la base de datos de FireStore para guardar
        val db = FirebaseFirestore.getInstance()
        db.collection(Constantes.COLL_PRODUCTOS)
                //añado el id de forma manual al FirebaseDtore llevara el midmo id de la imagen
            .document(documentId)
            .set(producto)
            //.add(producto)
            .addOnSuccessListener {
                Toast.makeText(activity, "Producto añadido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error al insertar", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                habilitarUI(true)
                binding?.progressBar?.visibility = View.INVISIBLE
                //independiente si se añadio o no
                dismiss()
            }
    }

    private fun actualizar(producto: Producto){
        //Instanciamos la base de datos de FireStore para guardar
        val db = FirebaseFirestore.getInstance()
        producto.id?.let { id ->
            db.collection(Constantes.COLL_PRODUCTOS)
                .document(id)
                .set(producto)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    habilitarUI(true)
                    binding?.progressBar?.visibility = View.INVISIBLE
                    //independiente si se añadio o no
                    dismiss()
                }
        }

    }

    private fun habilitarUI(habilitar: Boolean){
        botonPositivo?.isEnabled = habilitar
        botonNegativo?.isEnabled = habilitar
        binding?.let {
            with(it){
                etNombre.isEnabled = habilitar
                etDescripcion.isEnabled = habilitar
                etCantidad.isEnabled = habilitar
                etPrecio.isEnabled = habilitar
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}