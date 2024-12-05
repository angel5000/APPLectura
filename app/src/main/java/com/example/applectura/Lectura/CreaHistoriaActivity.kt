package com.example.applectura.Lectura

import android.annotation.SuppressLint
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
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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
    private var imagenSeleccion2: Bitmap? = null
    val SELECT_IMAGE_REQUEST = 1
    val SELECT_IMAGE_REQUESTCAP = 2
    var idhist=0L
    private lateinit var buttonAgregarCapitulo : Button
    private lateinit var  checkBox: CheckBox
    private var isHistoriaGuardada = false
    private lateinit var btnGuardarCap:Button
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_historia)
        val dbHelper = DatabaseHelperCrear(this)

        val contenedorCapitulos = findViewById<LinearLayout>(R.id.contenedorCapitulos)
        val  editTextTitulo = findViewById<EditText>(R.id.editTextTitulo)
        val  editTextSinopsis = findViewById<EditText>(R.id.editTextSinopsis)
        val spinnerEstado= findViewById<Spinner>(R.id.spinnerEstado)
        val  editTextGenero = findViewById<EditText>(R.id.editTextGenero)
        val  imagenSeleccionada = findViewById<Button>(R.id.buttonSeleccionarPortada)
        val  btnGuardarHistoria = findViewById<Button>(R.id.btguardahist)
        buttonAgregarCapitulo = findViewById(R.id.buttonAgregarCapitulo)
          checkBox = findViewById(R.id.checkbt)
        // Infla el diseño donde está el botón
       // val inflater = LayoutInflater.from(this)


// Configura el evento del botón

        buttonAgregarCapitulo.isEnabled = false

        // Configurar el evento del CheckBox
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            toggleAgregarCapButton(isChecked)
            if (checkBox.isChecked) {
                buttonAgregarCapitulo.isEnabled = true
            }else{
                buttonAgregarCapitulo.isEnabled = false
            }
        }




            imagenSeleccionada.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, SELECT_IMAGE_REQUEST)
            }



        buttonAgregarCapitulo.setOnClickListener {


            // Inflar el diseño del capítulo
            val inflater = LayoutInflater.from(this)
           // val nuevoCapitulo = inflater.inflate(R.layout.creahistdat, null)
            val layoutCapitulo = inflater.inflate(R.layout.creahistdat, null)

// Encuentra el botón en el diseño inflado
            btnGuardarCap = layoutCapitulo.findViewById(R.id.btAgregaCap)
            val edittextnumcap  = layoutCapitulo.findViewById<EditText>(R.id.editTextNumeroCapitulo)
            val editexttitucap =  layoutCapitulo.findViewById<EditText>(R.id.editTextTituloCapitulo)
            val edittextcontcap = layoutCapitulo.findViewById<EditText>(R.id.editTextContenidoCapitulo)
            val btnGuardarportCap = layoutCapitulo.findViewById<Button>(R.id.buttonSeleccionarPortadaCapitulo)
            val edittextseccioncap = layoutCapitulo.findViewById<EditText>(R.id.editTextSeccionCapitulo)


            if( isHistoriaGuardada ){
                btnGuardarCap.isEnabled=true
            }else{
                btnGuardarCap.isEnabled=false
            }

            // Añadir al contenedor
            btnGuardarportCap.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, SELECT_IMAGE_REQUESTCAP)
            }
            btnGuardarCap.setOnClickListener {
                // Acción al presionar el botón
                Toast.makeText(this, "Botón Guardar Capítulo presionado", Toast.LENGTH_SHORT).show()

                // Ejemplo: Llamar a la función para guardar un capítulo
                val idHistoria = idhist // Asegúrate de pasar el ID de la historia real
                val numeroCapitulo = edittextnumcap.text.toString().toInt()
                val titulo = editexttitucap.text.toString()
                val contenido = edittextcontcap.text.toString()
                val portada: ByteArray? =  convertirImagenABlobCap( imagenSeleccion2)
                val seccion = edittextseccioncap.text.toString().toInt()
                val dbHelper = DatabaseHelperCrear(this)
                val resultado = dbHelper.CrearCapitulo(numeroCapitulo, titulo, contenido, portada, idHistoria,seccion)

                if (resultado != -1L) {
                    btnGuardarCap.isEnabled=false
                    btnGuardarCap.text="Capitulo guardado"
                    Toast.makeText(this, "Capítulo guardado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al guardar el capítulo", Toast.LENGTH_SHORT).show()
                }
            }

            contenedorCapitulos.addView(layoutCapitulo)
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
                    guardarHistoria()
                    Toast.makeText(this, "Historia creada con éxito (ID: $idHistoria)", Toast.LENGTH_SHORT).show()
                    idhist=idHistoria
                    btnGuardarCap.isEnabled=true
                    btnGuardarHistoria.isEnabled=false;
                    btnGuardarHistoria.text="Hisotoria Guardada"

                } else {
                    Toast.makeText(this, "Error al crear la historia", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Los campos Título y Sinopsis son obligatorios", Toast.LENGTH_SHORT).show()

            }
        }

            //////

    }
    private fun guardarHistoria() {
        val titulo = findViewById<EditText>(R.id.editTextTitulo).text.toString()
        val sinopsis = findViewById<EditText>(R.id.editTextSinopsis).text.toString()

        if (titulo.isNotEmpty() && sinopsis.isNotEmpty()) {
            isHistoriaGuardada = true // Marcar como guardada
            Toast.makeText(this, "Historia guardada exitosamente", Toast.LENGTH_SHORT).show()

            // Habilitar el botón de agregar capítulo si el checkbox está marcado

        } else {
            Toast.makeText(this, "Por favor, completa todos los campos para guardar la historia", Toast.LENGTH_SHORT).show()
        }
    }
    private fun toggleAgregarCapButton(isChecked: Boolean) {
        // Activar o desactivar solo si la historia ya fue guardada
        if (isHistoriaGuardada) {
            buttonAgregarCapitulo.isEnabled = isChecked
        } else {
            buttonAgregarCapitulo.isEnabled = false
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            val bitmap = uri?.let {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            when (requestCode) {
                SELECT_IMAGE_REQUEST -> {
                    if (bitmap != null) {
                        imagenSeleccion = bitmap

                    }
                }
                SELECT_IMAGE_REQUESTCAP-> {
                    if (bitmap != null) {
                        imagenSeleccion2 = bitmap

                    }
                }
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
    private fun convertirImagenABlobCap(bitmap: Bitmap?): ByteArray? {
        return bitmap?.let {
            val stream2 = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            stream2.toByteArray()
        }
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

    fun CrearCapitulo(
        numeroCapitulo: Int,
        titulo: String,
        contenido: String,
        portada: ByteArray?,
        idHistoria: Long,
        seccion: Int
    ): Long {
        val db = this.writableDatabase

        // Prepara los valores para la inserción
        val contentValues = ContentValues().apply {
            put("numeroCapitulo", numeroCapitulo)
            put("titulo", titulo)
            put("contenido", contenido)
            put("portada", portada)
            put("idHistoria", idHistoria)
            put("Seccion", seccion)
        }

        // Inserta el capítulo en la base de datos
        return db.insert("Capitulo", null, contentValues)
    }


    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}