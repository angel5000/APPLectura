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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.example.applectura.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class RedaccionActivity : AppCompatActivity() {
    var iddatos=0
    private var imagenSeleccion: Bitmap? = null
    val SELECT_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redaccion)
        val itemId = intent.getIntExtra("ITEM_IDRED", -1) // Recuperar el ID que fue enviado
        if (itemId != -1) {
            iddatos = itemId
        }
        Toast.makeText(this, "id: ${iddatos}", Toast.LENGTH_SHORT).show()
        val dbHelper = DatabaseHelper3(this)
        val capitulos = dbHelper.obtenerCapitulos(iddatos)

        val gridView = findViewById<GridView>(R.id.gridredaccion)
        val adapter = CapituloAdapter(this, capitulos)
        gridView.adapter = adapter


       // val titulo = intent.getStringExtra("titulo") ?: "Título no disponible"
      //  val textti = findViewById<TextView>(R.id.txttitulo)
      //  textti.text = titulo

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
                imagenSeleccion = bitmap
                var adapt=CapituloAdapter(this)
                adapt.onImageSelected(imagenSeleccion!!)
                Toast.makeText(this, "images ${imagenSeleccion}", Toast.LENGTH_SHORT).show()
            }
        }
    }




}

class DatabaseHelper3(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME =
            "LecturaAPPBD.db" // Nombre del archivo de base de datos
        private const val DATABASE_VERSION = 2
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

    data class Capitulo(
        val idCapitulo: Int,
        val numeroCapitulo: Int,
        val titulo: String,
        var contenido: String,
        var portada: ByteArray? // BLOB para la imagen, puede ser nulo
    )

    @SuppressLint("Range")
    fun obtenerCapitulos(idHistoria: Int): MutableList<Capitulo> {
        val info = mutableListOf<Capitulo>()
        val db = this.readableDatabase

        // Realizamos una sola consulta para obtener ambos campos: numeroCapitulo y titulo
        val cursor = db.rawQuery(
            "SELECT idCapitulo,numeroCapitulo, titulo, contenido, portada, seccion FROM Capitulo WHERE idHistoria =?",
            arrayOf(idHistoria.toString()) // Pasar el parámetro como un array de Strings
        )
        if (cursor.moveToFirst()) {
            do {
                val idCapitulo = cursor.getInt(cursor.getColumnIndexOrThrow("idCapitulo"))
                val numeroCapitulo = cursor.getInt(cursor.getColumnIndexOrThrow("numeroCapitulo"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val contenido = cursor.getString(cursor.getColumnIndexOrThrow("contenido"))
                val portada: ByteArray? = cursor.getBlob(cursor.getColumnIndexOrThrow("portada"))

                if (portada != null) {
                    Log.d("DatabaseDebug", "Tamaño del BLOB: ${portada.size}")
                } else {
                    Log.d("DatabaseDebug", "El BLOB 'portada' es null")
                }

                Log.d("DatabaseDebug", "content: ${contenido}")
                Log.d("DatabaseDebug", "id: ${idCapitulo}")

                // Agregar al listado, pasando null si portada es null
                info.add(Capitulo(idCapitulo, numeroCapitulo, titulo, contenido, portada))

            } while (cursor.moveToNext())

        }
       // Log.e("DatabaseError", "error")
        cursor.close()
        db.close()
        return info
    }
    /*    val capitulos = mutableListOf<Capitulo>()
        val db = this.readableDatabase

        // Modificar el rawQuery para usar el parámetro idHistoria
        val cursor = db.rawQuery(
            "SELECT numeroCapitulo, titulo, contenido, portada FROM Capitulo WHERE idHistoria =?",
            arrayOf(idHistoria.toString()) // Pasar el parámetro como un array de Strings
        )

        if (cursor.moveToFirst()) {
            do {
                val numeroCapitulo = cursor.getInt(cursor.getColumnIndexOrThrow("numeroCapitulo"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val contenido = cursor.getString(cursor.getColumnIndexOrThrow("contenido"))
                val portada = cursor.getBlob(cursor.getColumnIndexOrThrow("portada")) // Puede ser nulo

                capitulos.add(Capitulo(numeroCapitulo, titulo, contenido, portada))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return capitulos*/






    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}
class CapituloAdapter(private val context: Context, private val capitulos: List<DatabaseHelper3.Capitulo> = emptyList())  : BaseAdapter() {
        private val SELECT_IMAGE_REQUEST = 100
    private var imagenSeleccion: Bitmap? = null
    override fun getCount(): Int = capitulos.size

    override fun getItem(position: Int): DatabaseHelper3.Capitulo = capitulos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val capitulo = getItem(position)
        val view = LayoutInflater.from(context).inflate(R.layout.redaccion, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.imageContent)
        val cardView = view.findViewById<CardView>(R.id.cardcont)
        val textView = view.findViewById<TextView>(R.id.txtcont)
        val texttitulo = view.findViewById<TextView>(R.id.txttitulo)

        // Configura la portada del capítulo si está disponible
        if (capitulo.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(capitulo.portada, 0, capitulo.portada!!.size)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }

        textView.text = "${capitulo.contenido}"
       // texttitulo.text = if (capitulo.titulo.isNotEmpty()) "Capitulo: ${capitulo.numeroCapitulo}\n"+capitulo.titulo else ""
        texttitulo.text = if (capitulo.numeroCapitulo == 0) {
            capitulo.titulo // Solo muestra el título si el número del capítulo es 0
        } else {
            if (capitulo.titulo.isNotEmpty()) {
                "Capítulo: ${capitulo.numeroCapitulo}\n${capitulo.titulo}"
            } else {
                "" // Si el título está vacío, no muestra nada
            }
        }

//texttitulo.text="Capitulo: ${capitulo.numeroCapitulo}\n${capitulo.titulo}"
        // Configura el evento de larga pulsación
        cardView.setOnLongClickListener {
            showPopupMenu(cardView, capitulo, textView, imageView)
            true
        }

        return view
    }

    private fun showPopupMenu(cardView: View, capitulo: DatabaseHelper3.Capitulo, textView: TextView, imageView: ImageView) {
        val popupMenu = PopupMenu(context, cardView)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_edit -> {
                    showEditDialog(capitulo, textView, imageView)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun showEditDialog(capitulo: DatabaseHelper3.Capitulo, textView: TextView, imageView: ImageView) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_content, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextContent)
        val buttonLoadImage = dialogView.findViewById<Button>(R.id.buttonLoadImage)

        // Configura el texto inicial
        editText.setText(capitulo.contenido)

        // Crea el Dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Editar Contenido")
            .setPositiveButton("Guardar") { _, _ ->
                // Actualiza el contenido en el objeto y el TextView
                capitulo.contenido = editText.text.toString()
                textView.text = "${capitulo.titulo}\n${capitulo.contenido}"

                // Llama al método para guardar en la base de datos
                updateCapituloInDatabase(capitulo)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        // Configura el botón para cargar imagen
        buttonLoadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, SELECT_IMAGE_REQUEST)
            }
        }

        dialog.show()
    }

    fun onImageSelected(bitma: Bitmap) {



            imagenSeleccion = bitma



    }
    private fun convertirImagenABlob(bitmap: Bitmap?): ByteArray? {
        return bitmap?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }

    private fun updateCapituloInDatabase(capitulo: DatabaseHelper3.Capitulo) {
        val databaseHelper = DatabaseHelper3(context)
        val db = databaseHelper.writableDatabase

        val contentValues = ContentValues().apply {
            put("titulo", capitulo.titulo)
            put("contenido", capitulo.contenido)
            put("portada", capitulo.portada)
            // Solo agregar la portada si no es null
           /* val portada: ByteArray? = convertirImagenABlob( imagenSeleccion)
            capitulo.portada=portada
            if (capitulo.portada != null) {



            }*/
        }

        val rowsUpdated = db.update(
            "Capitulo",
            contentValues,
            "idCapitulo = ?",
            arrayOf(capitulo.idCapitulo.toString()),

        )

        if (rowsUpdated > 0) {
            Toast.makeText(context, "Capítulo actualizado correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al actualizar el capítulo", Toast.LENGTH_SHORT).show()
        }

        db.close()
    }


}
