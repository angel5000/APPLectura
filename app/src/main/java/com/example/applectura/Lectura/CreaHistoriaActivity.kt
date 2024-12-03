package com.example.applectura.Lectura

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.applectura.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class CreaHistoriaActivity : AppCompatActivity() {
    private var imagenSeleccion: Bitmap? = null
    val SELECT_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_historia)
        val dbHelper = DatabaseHelperCrear(this)
        val buttonAgregarCapitulo = findViewById<Button>(R.id.buttonAgregarCapitulo)
        val contenedorCapitulos = findViewById<LinearLayout>(R.id.contenedorCapitulos)
        val  editTextTitulo = findViewById<EditText>(R.id.editTextTitulo)
        val  editTextSinopsis = findViewById<EditText>(R.id.editTextSinopsis)
        val spinnerEstado= findViewById<Spinner>(R.id.spinnerEstado)
        val  editTextGenero = findViewById<EditText>(R.id.editTextGenero)
        val  imagenSeleccionada = findViewById<Button>(R.id.buttonSeleccionarPortada)
        val  btnGuardarHistoria = findViewById<Button>(R.id.btguardahist)


        imagenSeleccionada.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_IMAGE_REQUEST)
        }

        buttonAgregarCapitulo.setOnClickListener {
            // Inflar el diseño del capítulo
            val inflater = LayoutInflater.from(this)
            val nuevoCapitulo = inflater.inflate(R.layout.creahistdat, null)

            // Añadir al contenedor
            contenedorCapitulos.addView(nuevoCapitulo)
        }

        btnGuardarHistoria.setOnClickListener {
            val titulo = editTextTitulo.text.toString()
            val sinopsis = editTextSinopsis.text.toString()
            val estado = spinnerEstado.selectedItem.toString() // Asumiendo que es un Spinner
            val genero = editTextGenero.text.toString()
            val portada: ByteArray? = convertirImagenABlob( imagenSeleccion) // Método para convertir imagen a BLOB
            val idUsuario = 1 // Supongamos que el usuario actual tiene el ID 1

            if (titulo.isNotEmpty() && sinopsis.isNotEmpty()) {
                val idHistoria = dbHelper.CrearHistoria(
                    titulo = titulo,
                    sinopsis = sinopsis,
                    estado = estado,
                    portada = portada,
                    idUsuario = idUsuario,
                    genero = genero
                )

                if (idHistoria != -1L) {
                    Toast.makeText(this, "Historia creada con éxito (ID: $idHistoria)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al crear la historia", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Los campos Título y Sinopsis son obligatorios", Toast.LENGTH_SHORT).show()

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            val bitmap = uri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
            if (bitmap != null) {
                imagenSeleccion = bitmap // Almacena el Bitmap seleccionado
               // findViewById<ImageView>(R.id).setImageBitmap(bitmap) // Muestra la imagen en el ImageView
            }
        }
    }

    private fun convertirImagenABlob(bitmap: Bitmap?): ByteArray? {
        return bitmap?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
private fun convertirImagenABlob(bitmap: Bitmap?): ByteArray? {
    return bitmap?.let {
        val stream = ByteArrayOutputStream()
        it.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }
}

class DatabaseHelperCrear(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    fun CrearHistoria(
        titulo: String,
        sinopsis: String,
        estado: String = "borrador",
        portada: ByteArray? = null,
        idUsuario: Int,
        genero: String?
    ): Long {
        val db = this.writableDatabase

        // Prepara los valores para la inserción
        val contentValues = ContentValues().apply {
            put("titulo", titulo)
            put("sinopsis", sinopsis)
            put("estado", estado)
            put("portada", portada)
            put("idUsuario", idUsuario)
            put("genero", genero)
        }

        // Inserta los datos en la tabla Historia
        val result = db.insert("Historia", null, contentValues)

        // Cierra la base de datos
        db.close()

        return result // Devuelve el ID del registro insertado o -1 si falla
    }



    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}