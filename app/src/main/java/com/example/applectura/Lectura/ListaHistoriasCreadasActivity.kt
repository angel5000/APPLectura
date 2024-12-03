package com.example.applectura.Lectura

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaHistoriasCreadasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_historia)
        val btfloat = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

// Set Dashboard selected
        bottomNavigationView.selectedItemId = R.id.navigation_agregar

// Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_principal -> {
                    val intent = Intent(applicationContext, PrincipalActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(5, 1)
                    true
                }
                R.id.navigation_agregar -> true
                R.id.navigation_agregar -> {
                    val intent = Intent(applicationContext, ListaHistoriasCreadasActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(1, 1)
                    true
                }
                else -> false
            }
        }

            btfloat.setOnClickListener {
                val intent = Intent(this, CreaHistoriaActivity::class.java)
                startActivity(intent)

            }


    }
}