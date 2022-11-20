package com.example.firebasekotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hace referencia a la raiz de la base de datos
        val database = Firebase.database.reference

        val listener = object : ValueEventListener{

            // en este metodo se ven los cambios en vivo de la base de datos
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(String::class.java)
                    findViewById<TextView>(R.id.textViewData).text = "Remote Firebase: $data"
                }else{
                    findViewById<TextView>(R.id.textViewData).text = "Ruta sin datos"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // este error dara cuando no se puede leer los datos de la base de datos
                Toast.makeText(this@MainActivity, "error al leer datos", Toast.LENGTH_SHORT).show()
            }

        }

        val dataRef = database.child("hola_firebase").child("data")
        dataRef.addValueEventListener(listener)

        // boton para introducir datos a la base de datos en realtime firebase
        findViewById<Button>(R.id.buttonEnviar).setOnClickListener {
            val data = findViewById<TextInputEditText>(R.id.editTextData).text.toString()
            dataRef.setValue(data)
        }

        findViewById<Button>(R.id.buttonEnviar).setOnLongClickListener{
            dataRef.removeValue()
            true
        }
    }
}