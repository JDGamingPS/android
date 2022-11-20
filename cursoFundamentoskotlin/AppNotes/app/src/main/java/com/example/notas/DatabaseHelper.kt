package com.example.notas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, Constantes.DATABASE_NAME,
    null, Constantes.DATABASE_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        // definir las tablas que van a ir interactuando
        val crearTabla = "CREATE TABLE ${Constantes.ENTITY_NOTA} (" +
                "${Constantes.PROPERTY_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Constantes.PROPERTY_DESCRIPCION} VARCHAR(60), " +
                "${Constantes.PROPERTY_IS_FINISHED} BOOLEAN)"

        db?.execSQL(crearTabla)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun obtenerTodasLasNotas(): MutableList<Nota>{
        val notas: MutableList<Nota> = mutableListOf()
        // de solo lectura
        val database = this.readableDatabase
        val consulta = "SELECT * FROM ${Constantes.ENTITY_NOTA}"

        //ejecutar esa sentencia
        val resultado = database.rawQuery(consulta, null)

        //evaluar nuestra el resultado, en vase a eso empezar a construir nuestros objetos de tipo nota
        if (resultado.moveToFirst()){
            do {
                val nota = Nota()
                nota.id = resultado.getLong(resultado.getColumnIndexOrThrow(Constantes.PROPERTY_ID))
                nota.descripcion = resultado.getString(resultado.getColumnIndexOrThrow(Constantes.PROPERTY_DESCRIPCION))
                nota.estaFinalizado= resultado.getInt(resultado.getColumnIndexOrThrow(Constantes.PROPERTY_IS_FINISHED)) == Constantes.TRUE // para el boolean

                notas.add(nota)
            } while (resultado.moveToNext())
        }

        return notas
    }

    fun insertarNota(nota: Nota): Long {
        val database = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(Constantes.PROPERTY_DESCRIPCION, nota.descripcion)
            put(Constantes.PROPERTY_IS_FINISHED, nota.estaFinalizado)
        }

        val resultadoId = database.insert(Constantes.ENTITY_NOTA, null, contentValues)
        return resultadoId
    }

    fun actualizarNota(nota: Nota): Boolean{
        val database = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(Constantes.PROPERTY_DESCRIPCION, nota.descripcion)
            put(Constantes.PROPERTY_IS_FINISHED, nota.estaFinalizado)
        }
        val resultado = database.update(Constantes.ENTITY_NOTA, contentValues,
            "${Constantes.PROPERTY_ID} = ${nota.id}",
            null)

        return resultado == Constantes.TRUE

    }

    fun borrarNota(nota: Nota): Boolean{
        val database = this.writableDatabase
        val resultado = database.delete(Constantes.ENTITY_NOTA,
            "${Constantes.PROPERTY_ID} = ${nota.id}",
            null)

        return resultado == Constantes.TRUE
    }

}