package com.example.applectura.Lectura

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.example.applectura.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreaHistoriaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_historia)
        val buttonAgregarCapitulo = findViewById<Button>(R.id.buttonAgregarCapitulo)
        val contenedorCapitulos = findViewById<LinearLayout>(R.id.contenedorCapitulos)


        buttonAgregarCapitulo.setOnClickListener {
            // Inflar el diseño del capítulo
            val inflater = LayoutInflater.from(this)
            val nuevoCapitulo = inflater.inflate(R.layout.creahistdat, null)

            // Añadir al contenedor
            contenedorCapitulos.addView(nuevoCapitulo)
        }


    }
}