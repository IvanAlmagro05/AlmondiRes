package com.example.almondires.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.almondires.R
import com.example.almondires.database.DBHelper
import java.util.Locale

class FragmentLogin : Fragment(R.layout.fragment_login) {

    private var isFirstSelection = true // Bandera para evitar el bucle al iniciar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DBHelper(requireContext())

        // Configura el Spinner de idiomas
        val spinner = view.findViewById<Spinner>(R.id.spinnerIdioma)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.idiomas,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Gestiona la selección de idioma en el Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                if (position == 0) return
                val locale = when (position) {
                    1 -> "es"
                    2 -> "en"
                    3 -> "fr"
                    4 -> "de"
                    5 -> "zh"
                    else -> return
                }
                setLocale(locale)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Valida credenciales contra la DB e inicia sesión
        view.findViewById<Button>(R.id.btnIniciar).setOnClickListener {
            val usuario = view.findViewById<EditText>(R.id.edtUsuario).text.toString()
            val password = view.findViewById<EditText>(R.id.edtContrasena).text.toString()
            val userId = db.validarUsuario(usuario, password)
            if (userId != -1L) {
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_login), Toast.LENGTH_SHORT).show()
            }
        }

        // Navega a la pantalla de registro
        view.findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentRegister())
                .addToBackStack(null)
                .commit()
        }
    }

    // Cambia el idioma de la aplicación y reinicia la actividad para aplicar cambios
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }
}
