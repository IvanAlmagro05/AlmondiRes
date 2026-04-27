package com.example.almondires.activities

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.almondires.R
import com.example.almondires.adapters.AlojamientoAdapter
import com.example.almondires.database.DBHelper
import com.example.almondires.models.Alojamientos
import java.util.regex.Pattern

class FragmentRegister : Fragment(R.layout.fragment_register) {

    private lateinit var db: DBHelper
    private val alojamientosLista = mutableListOf<Alojamientos>()
    private lateinit var adapter: AlojamientoAdapter

    // Inicializa el fragmento y configura la interfaz de registro
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBHelper(requireContext())

        // Configura la lista de alojamientos temporales
        val rv = view.findViewById<RecyclerView>(R.id.rvAlojamientoTemporal)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = AlojamientoAdapter(alojamientosLista){
            alojamiento ->
            val position = alojamientosLista.indexOf(alojamiento)
            if(position!=-1){
                alojamientosLista.removeAt(position)
                adapter.notifyItemRemoved(position)
            }

        }
        rv.adapter = adapter

        // Referencias a los campos de usuario
        val edtUsuario = view.findViewById<EditText>(R.id.edtUsuarioReg)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailReg)
        val edtPass = view.findViewById<EditText>(R.id.edtContrasenaReg)
        val edtRepitePass = view.findViewById<EditText>(R.id.edtRepiteContrasenaReg)
        val edtTelf = view.findViewById<EditText>(R.id.edtTelefonoReg)

        // Abre un diálogo para añadir un alojamiento a la lista
        view.findViewById<Button>(R.id.btnAddAlojamiento).setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_alojamiento, null)
            val edtPrecio = dialogView.findViewById<EditText>(R.id.edtPrecioAlojamientoDialog)

                // Filtro para limitar a 2 decimales
                edtPrecio.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                    val nuevoTexto = dest.toString() + source.toString()
                    val pattern = Pattern.compile("^\\d*(\\.\\d{0,2})?$")
                    val matcher = pattern.matcher(nuevoTexto)
                    if (!matcher.matches()) "" else null
                })

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.btn_add_alojamiento))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                    val nombre = dialogView.findViewById<EditText>(R.id.edtNombreAlojamientoDialog).text.toString()
                    val precio = edtPrecio.text.toString().toDoubleOrNull() ?: 0.0
                    
                    if (nombre.isNotEmpty()) {
                        alojamientosLista.add(Alojamientos(nombre = nombre, precio = precio))
                        adapter.notifyItemInserted(alojamientosLista.size - 1)
                    }
                }
                .setNegativeButton(getString(R.string.boton_cancelar), null)
                .show()
        }

        // Valida los datos y guarda el usuario y sus alojamientos en la DB
        view.findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val user = edtUsuario.text.toString()
            val email = edtEmail.text.toString()
            val pass = edtPass.text.toString()
            val repitePass = edtRepitePass.text.toString()
            val telf = edtTelf.text.toString()

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || telf.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != repitePass) {
                Toast.makeText(requireContext(), getString(R.string.error_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoUsuarioId = db.insertarUsuario(user, email, pass, telf)

            if (nuevoUsuarioId != -1L) {
                for (aloj in alojamientosLista) {
                    db.insertarAlojamiento(aloj.nombre, aloj.precio, nuevoUsuarioId)
                }
                Toast.makeText(requireContext(), getString(R.string.registro_completado), Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_registro), Toast.LENGTH_SHORT).show()
            }
        }

        // Regresa a la pantalla de Login
        view.findViewById<Button>(R.id.btnAtras).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
