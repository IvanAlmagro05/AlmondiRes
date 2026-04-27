package com.example.almondires.activities

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.almondires.R
import com.example.almondires.adapters.ReservaAdapter
import com.example.almondires.database.DBHelper
import com.example.almondires.models.Alojamientos
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FragmentReservas : Fragment(R.layout.fragment_reservas) {

    private lateinit var db: DBHelper
    private lateinit var recycler: RecyclerView
    private var currentUserId: Long = -1

    private val SMS_PERMISSION_CODE = 100

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())
        currentUserId = requireActivity().intent.getLongExtra("USER_ID", -1)

        recycler = view.findViewById(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        val btnReservas = view.findViewById<Button>(R.id.btnReserva)

        btnReservas.setOnClickListener {
            mostrarDialogoReserva()
        }

        cargarReservas()
    }

    private fun mostrarDialogoReserva() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reservas, null)
        val containerAlojamientos = dialogView.findViewById<LinearLayout>(R.id.containerAlojamientos)
        
        val misAlojamientos = db.obtenerAlojamientosPorUsuario(currentUserId)
        val checkBoxes = mutableListOf<Pair<Alojamientos, CheckBox>>()

        for (alojamiento in misAlojamientos) {
            val cb = CheckBox(requireContext())
            cb.text = "${alojamiento.nombre} (${alojamiento.precio}€)"
            containerAlojamientos.addView(cb)
            checkBoxes.add(alojamiento to cb)
        }

        val btnFechaInicio = dialogView.findViewById<Button>(R.id.btnFechaIni)
        val btnFechaFin = dialogView.findViewById<Button>(R.id.btnFechaFin)
        val edtNombre = dialogView.findViewById<EditText>(R.id.edtNombre)
        val txtNumNoches = dialogView.findViewById<TextView>(R.id.txtNumNoches)
        val edtTelf = dialogView.findViewById<EditText>(R.id.edtTelf)

        fun actualizarNoches() {
            val inicio = btnFechaInicio.text.toString()
            val fin = btnFechaFin.text.toString()
            if (inicio.contains("/") && fin.contains("/")) {
                val noches = calcularNoches(inicio, fin)
                txtNumNoches.text = noches.toString()
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = edtNombre.text.toString()
                val fechaIni = btnFechaInicio.text.toString()
                val fechaFinal = btnFechaFin.text.toString()
                val numNoches = txtNumNoches.text.toString().toIntOrNull() ?: 0
                val telefono = edtTelf.text.toString()

                if (!fechaIni.contains("/") || !fechaFinal.contains("/"))  {
                    Toast.makeText(requireContext(), R.string.campos_vacios, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if(numNoches <= 0){
                    Toast.makeText(requireContext(), getString(R.string.error_fecha_fin), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val alojamientosSeleccionados = checkBoxes.filter { it.second.isChecked }
                if (alojamientosSeleccionados.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.seleccionar_alojamiento), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                var precioBasePorNoche = 0.0
                for ((aloj, _) in alojamientosSeleccionados) {
                    precioBasePorNoche += aloj.precio
                }

                val precioTotal = precioBasePorNoche * numNoches

                if (nombre.isNotEmpty() && telefono.isNotEmpty()) {
                    if (db.fechaOcupada(fechaIni, fechaFinal, currentUserId)) {
                        Toast.makeText(requireContext(), getString(R.string.fecha_ocupada), Toast.LENGTH_LONG).show()
                    } else {
                        // Insertamos la reserva y obtenemos su ID
                        val reservaId = db.insertarReserva(
                            nombre,
                            fechaIni,
                            fechaFinal,
                            precioTotal,
                            telefono,
                            numNoches,
                            currentUserId
                        )

                        // Insertamos la relación con los alojamientos seleccionados
                        if (reservaId != -1L) {
                            for ((aloj, _) in alojamientosSeleccionados) {
                                db.insertarReservaAlojamiento(reservaId, aloj.id)
                            }
                        }

                        val mensajeSMS = getString(R.string.sms_reserva, fechaIni, fechaFinal, precioTotal.toString())

                        // 1. Enviamos al CLIENTE
                        enviarConfirmacionSMS(telefono, mensajeSMS)

                        // 2. Respaldo al DUEÑO
                        val usuarioActual = db.obtenerUsuario(currentUserId)
                        val telefonoRespaldo = if (usuarioActual != null && usuarioActual.telefono.isNotEmpty()) {
                            usuarioActual.telefono
                        } else {
                            "5554"
                        }
                        
                        enviarConfirmacionSMS(telefonoRespaldo, "AVISO: Reserva confirmada $fechaIni - $fechaFinal ($precioTotal€)")
                        
                        cargarReservas()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.boton_cancelar), null)
            .show()

        btnFechaInicio.setOnClickListener { mostrarDatePicker { btnFechaInicio.text = it; actualizarNoches() } }
        btnFechaFin.setOnClickListener { mostrarDatePicker { btnFechaFin.text = it; actualizarNoches() } }
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

    private fun mostrarDatePicker(onFecha: (String) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d -> onFecha("$d/${m + 1}/$y") }, 
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun cargarReservas() {
        val lista = db.obtenerReservasPorUsuario(currentUserId)
        recycler.adapter = ReservaAdapter(lista, db, currentUserId) { cargarReservas() }
    }
    
    
    private fun enviarConfirmacionSMS(telefono: String, mensaje: String) {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                
                smsManager.sendTextMessage(telefono.trim(), null, mensaje, null, null)
                
                // Mostramos el Toast de confirmación
                Toast.makeText(context, getString(R.string.confirmacion_reserva, telefono), Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.error_sms_reserva) + " ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        }
    }
}
