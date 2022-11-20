package com.example.notas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notas.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notasAdapater: NotaAdapater
    private lateinit var notesFinishedAdapater: NotaAdapater

    private lateinit var database: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DatabaseHelper(this)


        //Vincular la Vista en el recyclerView

        notasAdapater = NotaAdapater(mutableListOf(), this )
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            //adapter es propia de kotlin para enlazar notaAdapter
            adapter = notasAdapater
        }

        // para el segundo adaptador
        notesFinishedAdapater = NotaAdapater(mutableListOf(), this )
        binding.rvNotesFinished.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            //adapter es propia de kotlin para enlazar notaAdapter
            adapter = notesFinishedAdapater
        }

        //añadir note al adapter
        binding.btnAdd.setOnClickListener {
            if (binding.etDescripcion.text.toString().isNotBlank()){
                val nota = Nota(descripcion = binding.etDescripcion.text.toString().trim())
                nota.id = database.insertarNota(nota)
                if (nota.id != Constantes.ID_ERROR){
                    addNotaAutomatica(nota)
                    binding.etDescripcion.text?.clear()
                    //binding.etDescripcion.error = null
                    Snackbar.make(binding.root, "Operacion Exitosa", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(binding.root, "Error Al modificar la base de datos.", Snackbar.LENGTH_SHORT).show()
                }

            }else{
                binding.etDescripcion.error = getString(R.string.validation_field_required)
            }
        }

    }

    override fun onStart() {

        // corre antes de que se inicie la app
        super.onStart()
        obtenerDatos()
    }

    private fun obtenerDatos(){
        /*val data = mutableListOf(
            Nota(1, "Estudiar"),
            Nota(2, "Ir al marcado"),
            Nota(3, "Comprar arroz", true))*/

        //Repartir a cada uno de los adaptadores, dependiendo si esta, o no seleccionado

        val data = database.obtenerTodasLasNotas()

        data.forEach { nota ->
            addNotaAutomatica(nota)
        }
    }

    private fun addNotaAutomatica(nota: Nota) {
        if (nota.estaFinalizado){
            //notas finalizadas
            notesFinishedAdapater.add(nota)
        }else{
            // las notas pendientes
            notasAdapater.add(nota)
        }

    }

    private fun borrarNotaAuto(nota: Nota) {
        if (nota.estaFinalizado){
            notasAdapater.remove(nota)
        }else{
            notesFinishedAdapater.remove(nota)
        }


    }

    override fun onChecked(nota: Nota) {
        if (database.actualizarNota(nota)){
            borrarNotaAuto(nota)
            addNotaAutomatica(nota)
        }else{
            Snackbar.make(binding.root, "Error Al modificar la base de datos.", Snackbar.LENGTH_SHORT).show()
        }
    }

    // con un click largo borramos la nota del recyclerView
    override fun onLongClick(nota: Nota, adaptadorActual: NotaAdapater) {
        val constructor = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setPositiveButton(getString(R.string.dialog_ok), { dialogInterface, i ->
                if (database.borrarNota(nota)){
                    adaptadorActual.remove(nota)
                    Snackbar.make(binding.root, "Operacion Exitosa.", Snackbar.LENGTH_SHORT).show()
                }else{
                    Snackbar.make(binding.root, "Error Al modificar la base de datos.", Snackbar.LENGTH_SHORT).show()
                }

            })
            .setNegativeButton(getString(R.string.dialog_cancel), null)
        constructor.create().show()

    }


}