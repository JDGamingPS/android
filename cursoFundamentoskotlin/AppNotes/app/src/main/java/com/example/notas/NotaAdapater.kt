package com.example.notas

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.notas.databinding.ItemNotaBinding

class NotaAdapater(var notalist: MutableList<Nota>, private val listener: OnClickListener) : RecyclerView.Adapter<NotaAdapater.ViewHolder>(){

    private lateinit var contexto: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val view = LayoutInflater.from(contexto).inflate(R.layout.item_nota, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nota = notalist.get(position)

        holder.setListener(nota)

        holder.binding.tvDescripcion.text = nota.descripcion

        // ajuste de altura y visualizar -"estaFinalizado"

        holder.binding.cbFinish.isChecked = nota.estaFinalizado

        //modificar TextSize cuando esta en check
        if(nota.estaFinalizado){
            holder.binding.tvDescripcion.setTextSize(TypedValue.COMPLEX_UNIT_SP, contexto.resources.getInteger(R.integer.description_size_finished).toFloat())

        }else{
            // no modificar TextSize si no tiene un check
            holder.binding.tvDescripcion.setTextSize(TypedValue.COMPLEX_UNIT_SP, contexto.resources.getInteger(R.integer.description_size_default).toFloat())
        }

    }

    override fun getItemCount(): Int {
        return notalist.size
    }

    fun add(nota: Nota) {
        notalist.add(nota)

        //notifica al adaptador que hay un nuevo elemento y refresca la vista
        notifyDataSetChanged()
    }

    fun remove(nota: Nota) {
        notalist.remove(nota)

        //notifica al adaptador que hay un nuevo elemento y refresca la vista
        notifyDataSetChanged()
    }


    // esta clase nos va a permiter a inflar la vista item_nota
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemNotaBinding.bind(view)

        fun setListener(nota: Nota){
            // No ponemos el setOnChekedChangeListener { compountButton, b -> } porque al hacerlo tendra que volver a lanzar el onBind y tampoco notifica
            binding.cbFinish.setOnClickListener {
                nota.estaFinalizado = (it as CheckBox).isChecked
                //notifyDataSetChanged()
                listener.onChecked(nota)
            }
            binding.root.setOnLongClickListener {
                // solo es necesario poner this por que solo tiene una unica referencia al adaptador (notaAdapter,notaAdapterFinished )
                listener.onLongClick(nota, this@NotaAdapater)
                true
            }
        }
    }


}