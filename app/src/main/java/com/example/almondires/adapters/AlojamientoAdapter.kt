package com.example.almondires.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.almondires.R
import com.example.almondires.models.Alojamientos

class AlojamientoAdapter(
    private val lista: List<Alojamientos>,
    private val onDeleteClick: (Alojamientos) -> Unit
) :
    RecyclerView.Adapter<AlojamientoAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreAlojamiento)
        val precio: TextView = view.findViewById(R.id.txtPrecioAlojamiento)
        val btnBorrar : Button = view.findViewById(R.id.btnBorrarAlojamiento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alojamiento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nombre.text = item.nombre
        holder.precio.text = "${item.precio}€"

        holder.btnBorrar.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = lista.size
}
