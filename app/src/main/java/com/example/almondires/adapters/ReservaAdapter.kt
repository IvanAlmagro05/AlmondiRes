package com.example.almondires.adapters

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.almondires.R
import com.example.almondires.database.DBHelper
import com.example.almondires.models.Alojamientos
import com.example.almondires.models.Reservas
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReservaAdapter(
    private val reservas : MutableList<Reservas>,
    private val db: DBHelper,
    private val currentUserId: Long,
    private val refrescar:() -> Unit
) : RecyclerView.Adapter<ReservaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cliente: TextView = view.findViewById(R.id.txtNombre)
        val fechaIni: TextView = view.findViewById(R.id.txtFechaIni)
        val fechaFin: TextView = view.findViewById(R.id.txtFechaFin)
        val eliminar: Button = view.findViewById(R.id.btnCancelar)
        val editar: Button = view.findViewById(R.id.btnEditarReserva)
        val telefono : TextView = view.findViewById(R.id.txtTelefono)
        val precioTotal : TextView = view.findViewById(R.id.txtPrecioTotal)
        val txtNoches : TextView = view.findViewById(R.id.txtNoches)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservas, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount()= reservas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reserva = reservas[position]
        val context = holder.itemView.context

        holder.cliente.text = "${context.getString(R.string.nombre)} ${reserva.nombre}"
        holder.fechaIni.text = "${context.getString(R.string.fechaInicio)} ${reserva.fechaInicio}"
        holder.fechaFin.text = "${context.getString(R.string.fechaFin)} ${reserva.fechaFinal}"
        holder.telefono.text = "${context.getString(R.string.telefono)} ${reserva.telf}"
        holder.txtNoches.text = "${context.getString(R.string.noches)} ${reserva.noches}"
        val precioFormateado = String.format(Locale.getDefault(), "%.2f", reserva.precio)
        holder.precioTotal.text = "${context.getString(R.string.precioTotal)} ${precioFormateado}€"

        holder.eliminar.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirmacion, null)
            val builder = AlertDialog.Builder(context).setView(dialogView)
            val dialog = builder.create()

            dialogView.findViewById<Button>(R.id.btnDialogCancelar).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnDialogConfirmar).setOnClickListener {
                db.borrarReserva(reserva.id)
                refrescar()
                dialog.dismiss()
            }
            dialog.show()
        }

        holder.editar.setOnClickListener {
            mostrarDialogoEditar(reserva, holder)
        }
    }

    private fun mostrarDialogoEditar(reserva: Reservas, holder: ViewHolder) {
        val context = holder.itemView.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_reserva, null)
        
        val btnFechaInicio = dialogView.findViewById<Button>(R.id.btnFechaInicio)
        val btnFechaFin = dialogView.findViewById<Button>(R.id.btnFechaFinal)
        val edtNombre = dialogView.findViewById<EditText>(R.id.edtNombre)
        val txtNumNoches = dialogView.findViewById<TextView>(R.id.txtNumNoches)
        val edtTelf = dialogView.findViewById<EditText>(R.id.edtTelf)
        val containerAlojamientos = dialogView.findViewById<LinearLayout>(R.id.containerAlojamientos)

        // Cargar datos actuales
        edtNombre.setText(reserva.nombre)
        btnFechaInicio.text = reserva.fechaInicio
        btnFechaFin.text = reserva.fechaFinal
        txtNumNoches.text = reserva.noches.toString()
        edtTelf.setText(reserva.telf)

        val misAlojamientos = db.obtenerAlojamientosPorUsuario(currentUserId)
        val alojamientosActuales = db.obtenerAlojamientosPorReserva(reserva.id)
        val checkBoxes = mutableListOf<Pair<Alojamientos, CheckBox>>()

        for (alojamiento in misAlojamientos) {
            val cb = CheckBox(context)
            cb.text = "${alojamiento.nombre} (${alojamiento.precio}€)"
            cb.isChecked = alojamientosActuales.contains(alojamiento.id)
            containerAlojamientos.addView(cb)
            checkBoxes.add(alojamiento to cb)
        }

        fun actualizarNoches() {
            val inicio = btnFechaInicio.text.toString()
            val fin = btnFechaFin.text.toString()
            if (inicio.contains("/") && fin.contains("/")) {
                val noches = calcularNoches(inicio, fin)
                txtNumNoches.text = noches.toString()
            }
        }

        btnFechaInicio.setOnClickListener { mostrarDatePicker(context) { btnFechaInicio.text = it; actualizarNoches() } }
        btnFechaFin.setOnClickListener { mostrarDatePicker(context) { btnFechaFin.text = it; actualizarNoches() } }

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.editar_reserva))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.guardar)) { _, _ ->
                val nombre = edtNombre.text.toString()
                val fechaIni = btnFechaInicio.text.toString()
                val fechaFinal = btnFechaFin.text.toString()
                val numNoches = txtNumNoches.text.toString().toIntOrNull() ?: 0
                val telefono = edtTelf.text.toString()

                var precioBasePorNoche = 0.0
                val alojamientosSeleccionados = mutableListOf<Int>()
                for ((aloj, cb) in checkBoxes) {
                    if (cb.isChecked) {
                        precioBasePorNoche += aloj.precio
                        alojamientosSeleccionados.add(aloj.id)
                    }
                }

                val precioTotal = precioBasePorNoche * numNoches

                if (nombre.isNotEmpty()) {
                    db.actualizarReserva(reserva.id, nombre, fechaIni, fechaFinal, precioTotal, telefono, numNoches)
                    db.actualizarReservaAlojamientos(reserva.id, alojamientosSeleccionados)
                    refrescar()
                } else {
                    Toast.makeText(context, context.getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(context.getString(R.string.boton_cancelar), null)
            .show()
    }

    private fun calcularNoches(fechaIni: String, fechaFin: String): Int {
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        return try {
            val d1 = sdf.parse(fechaIni)!!
            val d2 = sdf.parse(fechaFin)!!
            val diff = d2.time - d1.time
            val nights = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            if (nights > 0) nights else 0
        } catch (e: Exception) { 0 }
    }

    private fun mostrarDatePicker(context: android.content.Context, onFecha: (String) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d -> onFecha("$d/${m + 1}/$y") }, 
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
}
