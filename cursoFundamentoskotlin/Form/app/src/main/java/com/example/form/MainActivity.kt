package com.example.form

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.example.form.databinding.ActivityMainBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Date Pdicker <- lanzador de fechas en un alaert Dialog
        binding.etFechaNac.setOnClickListener {
            val constructorPiker = MaterialDatePicker.Builder.datePicker()
            val picker = constructorPiker.build()

            // la fecha se calcula en milisegundos y el it es el dato en milisegundos
            picker.addOnPositiveButtonClickListener { tiempoenMilisegundos ->
                val datoStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC") // <- para arreglar el dia que se retrasa por un dia
                }.format(tiempoenMilisegundos)

                binding.etFechaNac.setText(datoStr.toString())
            }

            //muestra en un alert dialog la fecha
            picker.show(supportFragmentManager, picker.toString())
        }

        //Spinner de paises, autocomplete del spinner de paises

        val paises = arrayOf("Argentina", "Bolivia", "Chile",
            "Colombia", "Ecuador", "España", "Estados Unidos",
            "México", "Panama", "Peru", "Paraguay")

        val adaptadorPaises = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paises)
        binding.actvPaises.setAdapter(adaptadorPaises)
        binding.actvPaises.setOnItemClickListener { adapterView, view, i, l ->
            binding.etLugarNacimiento.requestFocus() //<- para que una ves selecciando el pais, el puntero este en lugar de nacimiento
            // seleccionar el pais
            Toast.makeText(this, paises.get(i), Toast.LENGTH_SHORT).show()

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.accion_enviar){
            if (validarNombres()){
                val nombre: String = binding.etNombre.text.toString().trim()
                val apellido = binding.etApellido.text.toString().trim()
                val altura = binding.etAltura.text.toString().trim()
                val fechaNac = binding.etFechaNac.text.toString().trim()
                val pais = binding.actvPaises.text.toString().trim()
                val lugarNac = binding.etLugarNacimiento.text.toString().trim()
                val notes = binding.etNotas.text.toString().trim()
                //Toast.makeText(this, "$nombre, $apellido", Toast.LENGTH_SHORT).show()

                // construyendo AlertDialogo
                val constructor: AlertDialog.Builder = AlertDialog.Builder(this)
                constructor.setTitle(getString(R.string.dialog_titulo))
                //muestra los datos en el dialogo
                constructor.setMessage(datosInsertados(nombre, apellido, altura, fechaNac, pais, lugarNac, notes))
                constructor.setPositiveButton("Limpiar", DialogInterface.OnClickListener { dialogInterface, i ->
                    with(binding){
                        etNombre.text?.clear()
                        etApellido.text?.clear()
                        etAltura.text?.clear()
                        etFechaNac.text?.clear()
                        actvPaises.text?.clear()
                        etLugarNacimiento.text?.clear()
                        etNotas.text?.clear()
                    }
                })
                constructor.setNegativeButton("Cancelar", null)

                val dialogo: AlertDialog = constructor.create()
                dialogo.show()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    // Funcion para mostrar lo datos en el dialog
    private fun datosInsertados(vararg fields: String): String{
        var resultado = ""
        fields.forEach { field ->
            if (field.isNotEmpty()){
                resultado = resultado + "$field\n"
            }

        }
        return resultado
    }

    private fun validarNombres():Boolean{
        var esValido = true

        // Validando la altura
        if (binding.etAltura.text.isNullOrEmpty()){
            binding.tilAltura.run {
                error = getString(R.string.help_required)
                requestFocus()
            }
            esValido = false
        } else if (binding.etAltura.text.toString().toInt() < 50) {
            binding.tilAltura.run {
                error = getString(R.string.help_min_estatura_valid)
                requestFocus()
            }
            esValido = false

        } else{
            binding.tilAltura.error = null
        }

        // validando el Apellido
        if (binding.etApellido.text.isNullOrEmpty()){
            binding.tilApellido.run {
                error = getString(R.string.help_required)
                requestFocus()
            }
            esValido = false
        }else{
            binding.tilApellido.error = null
        }

        // Validando el Nombre
        if (binding.etNombre.text.isNullOrEmpty()){

            binding.tilNombre.run{
                error = getString(R.string.help_required) //<- error, muestra el field en error
                requestFocus() // <- solicita el foco para que lo llene si o si
            }

            esValido = false
        }else{
            binding.tilNombre.error = null // <- desaparece el error en el TextField
        }

        return esValido
    }


}