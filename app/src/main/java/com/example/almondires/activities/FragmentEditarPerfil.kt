package com.example.almondires.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.almondires.R
import com.example.almondires.database.DBHelper

class FragmentEditarPerfil : Fragment(R.layout.fragment_editar_perfil) {

    private lateinit var db: DBHelper
    private var currentUserId: Long = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBHelper(requireContext())
        
        // Obtenemos el ID del usuario a editar
        currentUserId = arguments?.getLong("USER_ID") ?: -1L

        // Referencias a los campos del formulario
        val edtUsuario = view.findViewById<EditText>(R.id.edtUsuarioEdit)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailEdit)
        val edtPass = view.findViewById<EditText>(R.id.edtContrasenaEdit)
        val edtRepitePass = view.findViewById<EditText>(R.id.edtRepiteContrasenaEdit)
        val edtTelf = view.findViewById<EditText>(R.id.edtTelefonoEdit)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarPerfil)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelarEdit)

        // Cargamos los datos actuales del usuario para que el usuario pueda verlos antes de editar
        val usuario = db.obtenerUsuario(currentUserId)
        usuario?.let {
            edtUsuario.setText(it.usuario)
            edtEmail.setText(it.email)
            edtPass.setText(it.password)
            edtRepitePass.setText(it.password)
            edtTelf.setText(it.telefono)
        }

        // Lógica para guardar los cambios
        btnGuardar.setOnClickListener {
            val user = edtUsuario.text.toString()
            val email = edtEmail.text.toString()
            val pass = edtPass.text.toString()
            val repitePass = edtRepitePass.text.toString()
            val telf = edtTelf.text.toString()

            // Validaciones básicas: campos no vacíos y contraseñas coincidentes
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || telf.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != repitePass) {
                Toast.makeText(requireContext(), getString(R.string.error_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizamos en la base de datos
            val filasAfectadas = db.actualizarUsuario(currentUserId, user, email, pass, telf)

            if (filasAfectadas > 0) {
                Toast.makeText(requireContext(), getString(R.string.perfil_actualizado), Toast.LENGTH_SHORT).show()
                // Volvemos al fragmento anterior (Perfil)
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_perfil_actualizado), Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para cancelar y volver atrás sin guardar
        btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
