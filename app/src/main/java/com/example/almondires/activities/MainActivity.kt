package com.example.almondires.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.almondires.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var currentUserId: Long = -1

    // Inicializa la actividad y configura la navegación inferior
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recuperamos el ID del usuario que viene del Login
        currentUserId = intent.getLongExtra("USER_ID", -1)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Gestiona los clics en la barra de navegación para cambiar de fragmento
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reservas -> {
                    cambiarFragmento(FragmentReservas())
                    true
                }
                R.id.nav_calendario -> {
                    cambiarFragmento(FragmentCalendario())
                    true
                }
                R.id.nav_estadisticas -> {
                    val fragment = FragmentEstadisticas()
                    cambiarFragmento(fragment)
                    true
                }
                R.id.nav_perfil -> {
                    val fragment = FragmentPerfil()
                    val bundle = Bundle()
                    bundle.putLong("USER_ID", currentUserId)
                    fragment.arguments = bundle
                    cambiarFragmento(fragment)
                    true
                }
                else -> false
            }
        }

        // Carga la pantalla de reservas por defecto al iniciar
        if (savedInstanceState == null) {
            cambiarFragmento(FragmentReservas())
            bottomNav.selectedItemId = R.id.nav_reservas
        }
    }

    // Realiza el intercambio de fragmentos en el contenedor principal
    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
