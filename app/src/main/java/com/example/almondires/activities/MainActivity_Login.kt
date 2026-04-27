package com.example.almondires.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.almondires.R

class MainActivity_Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Carga LoginFragment la primera vez
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentLogin())
                .commit()
        }
    }
}
