package com.example.almondires.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.almondires.R
import com.example.almondires.adapters.AlojamientoAdapter
import com.example.almondires.database.DBHelper
import com.example.almondires.models.Alojamientos
import java.util.regex.Pattern

class FragmentPerfil : Fragment(R.layout.fragment_perfil) {

    private lateinit var db: DBHelper
    private var currentUserId: Long = -1
    private lateinit var adapter: AlojamientoAdapter
    private val listaAlojamientos = mutableListOf<Alojamientos>()

    // Inicializa el fragmento y carga los datos del usuario logueado
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())
        currentUserId = arguments?.getLong("USER_ID") ?: -1L

        val txtNombre = view.findViewById<TextView>(R.id.txtNombrePerfil)
        val txtEmail = view.findViewById<TextView>(R.id.txtEmailPerfil)
        val txtTelf = view.findViewById<TextView>(R.id.txtTelefonoPerfil)
        val rv = view.findViewById<RecyclerView>(R.id.rvMisAlojamientosPerfil)

        // Recupera la información del usuario desde la DB y la muestra
        val usuario = db.obtenerUsuario(currentUserId)
        usuario?.let {
            txtNombre.text = it.usuario
            txtEmail.text = it.email
            txtTelf.text = it.telefono
        }

        // Configura el RecyclerView para mostrar los alojamientos del usuario
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = AlojamientoAdapter(listaAlojamientos){
            alojamiento -> db.borrarAlojamiento(alojamiento.id)
            actualizarLista()
        }
        rv.adapter = adapter
        actualizarLista()

        // Abre un diálogo para añadir un nuevo alojamiento directamente a la DB
        view.findViewById<Button>(R.id.btnPerfilAddAlojamiento).setOnClickListener {
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
                        db.insertarAlojamiento(nombre, precio, currentUserId)
                        actualizarLista()
                    }
                }
                .setNegativeButton(getString(R.string.boton_cancelar), null)
                .show()
        }

        // Limpia la sesión y redirige al usuario a la pantalla de Login
        view.findViewById<Button>(R.id.btnCerrarSesionPerfil).setOnClickListener {
            val intent = Intent(requireContext(), MainActivity_Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // Navega al fragmento de edición de perfil
        view.findViewById<Button>(R.id.btnEditarPerfil).setOnClickListener {
            val fragment = FragmentEditarPerfil()
            val bundle = Bundle()
            bundle.putLong("USER_ID", currentUserId)
            fragment.arguments = bundle
            
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Permite volver atrás al perfil
                .commit()
        }
    }

    // Refresca la lista de alojamientos consultando de nuevo la base de datos
    private fun actualizarLista() {
        listaAlojamientos.clear()
        listaAlojamientos.addAll(db.obtenerAlojamientosPorUsuario(currentUserId))
        adapter.notifyDataSetChanged()
    }

    // Recargamos los datos cuando volvemos de editar el perfil
    override fun onResume() {
        super.onResume()
        val usuario = db.obtenerUsuario(currentUserId)
        usuario?.let {
            view?.findViewById<TextView>(R.id.txtNombrePerfil)?.text = it.usuario
            view?.findViewById<TextView>(R.id.txtEmailPerfil)?.text = it.email
            view?.findViewById<TextView>(R.id.txtTelefonoPerfil)?.text = it.telefono
        }
    }
}
