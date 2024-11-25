package com.example.applectura.Lectura

import android.content.Intent
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import com.example.applectura.R

class LecturaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lectura2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Habilitar el botón de retroceso si es necesario
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val dbHelper = DatabaseHelper(this)
        val txtNombreLectura = findViewById<TextView>(R.id.txtnomblect)

// Obtener datos de la base de datos
        val lectura = dbHelper.obtenerTituloPorId(1)
        txtNombreLectura.text = lectura

        val imageView: ImageView = findViewById(R.id.imagePortada)

// Obtener la imagen desde la base de datos
        val portadaBitmap = dbHelper.obtenerPortada(1) // Suponiendo que '1' es el id de la historia

// Verificar si la imagen existe y cargarla
        if (portadaBitmap != null) {
            imageView.setImageBitmap(portadaBitmap)
        }



    }




    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, PrincipalActivity::class.java)
        startActivity(intent)
        finish() // Finaliza la actividad actual para que no quede en la pila
        return true
    }

}
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME =
            "LecturaAPPBD.db" // Nombre del archivo de base de datos
        private const val DATABASE_VERSION = 1
    }

    private val databasePath = context.getDatabasePath(DATABASE_NAME).path

    init {
        if (!File(databasePath).exists()) {
            copiarBaseDatos(context)
        }
    }
    private fun copiarBaseDatos(context: Context) {
        val inputStream: InputStream = context.assets.open(DATABASE_NAME)
        val outputFile: String = databasePath
        val outputStream: OutputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
    fun obtenerTituloPorId(id: Int): String {
        val db = this.readableDatabase
        val query = "SELECT Titulo FROM Historia WHERE idHistoria = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
            cursor.close()
            titulo
        } else {
            cursor.close()
            "No encontrado"
        }
    }
    fun obtenerPortada(idHistoria: Int): Bitmap? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT portada FROM Historia WHERE idHistoria = ?", arrayOf(idHistoria.toString()))

        if (cursor != null && cursor.moveToFirst()) {
            // Obtener el índice de la columna 'portada'
            val columnaPortadaIndex = cursor.getColumnIndex("portada")

            // Verificar si la columna existe y contiene datos
            if (columnaPortadaIndex != -1) {
                val portadaBlob = cursor.getBlob(columnaPortadaIndex)

                // Verificar si 'portadaBlob' no es null antes de obtener su tamaño
                if (portadaBlob != null && portadaBlob.isNotEmpty()) {
                    return BitmapFactory.decodeByteArray(portadaBlob, 0, portadaBlob.size)
                } else {
                    Log.e("DatabaseError", "La columna 'portada' está vacía o es null.")
                    return null
                }
            } else {
                Log.e("DatabaseError", "La columna 'portada' no existe en la base de datos.")
                return null
            }
        }
        cursor.close()
        return null    }
    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}