package com.example.almondires.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.almondires.R
import com.example.almondires.database.DBHelper
import com.example.almondires.models.Alojamientos

class FragmentEstadisticas : Fragment(R.layout.fragment_estadisticas) {

    private lateinit var db: DBHelper
    private var currentUserId: Long = -1
    private lateinit var txtDiario: TextView
    private lateinit var txtMensual: TextView
    private lateinit var txtAnual: TextView
    private lateinit var spinner: Spinner
    private var listaAlojamientos = mutableListOf<Alojamientos>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())
        currentUserId = requireActivity().intent.getLongExtra("USER_ID", -1)

        txtDiario = view.findViewById(R.id.txtIngresoDiario)
        txtMensual = view.findViewById(R.id.txtIngresoMensual)
        txtAnual = view.findViewById(R.id.txtIngresoAnual)
        spinner = view.findViewById(R.id.spinnerEstadisticas)

        configurarSpinner()
    }

    private fun configurarSpinner() {
        listaAlojamientos = db.obtenerAlojamientosPorUsuario(currentUserId).toMutableList()
        
        // Creamos la lista de nombres para el Spinner, empezando por "General"
        val nombresPisos = mutableListOf<String>()
        nombresPisos.add(getString(R.string.general))
        nombresPisos.addAll(listaAlojamientos.map { it.nombre })

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresPisos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Opción General (todos los pisos)
                    actualizarEstadisticas(null)
                } else {
                    // Piso específico (restamos 1 porque el índice 0 es "General")
                    val alojamientoSeleccionado = listaAlojamientos[position - 1]
                    actualizarEstadisticas(alojamientoSeleccionado.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarEstadisticas(alojamientoId: Int?) {
        // Enviamos claves fijas al DBHelper para que no dependa del idioma
        val diario = db.obtenerIngresos(currentUserId, alojamientoId, "DIARIO")
        val mensual = db.obtenerIngresos(currentUserId, alojamientoId, "MENSUAL")
        val anual = db.obtenerIngresos(currentUserId, alojamientoId, "ANUAL")

        txtDiario.text = String.format("%.2f€", diario)
        txtMensual.text = String.format("%.2f€", mensual)
        txtAnual.text = String.format("%.2f€", anual)
    }
}
