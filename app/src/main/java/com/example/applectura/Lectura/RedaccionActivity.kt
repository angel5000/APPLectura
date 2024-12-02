package com.example.applectura.Lectura

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.example.applectura.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class RedaccionActivity : AppCompatActivity() {
    var iddatos=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redaccion)
        val itemId = intent.getIntExtra("ITEM_IDRED", -1) // Recuperar el ID que fue enviado
        if (itemId != -1) {
            iddatos = itemId
        }
        val dbHelper = DatabaseHelper3(this)
        val capitulos = dbHelper.obtenerCapitulos(iddatos)

        val gridView = findViewById<GridView>(R.id.gridredaccion)
        val adapter = CapituloAdapter(this, capitulos)
        gridView.adapter = adapter


        val titulo = intent.getStringExtra("titulo") ?: "Título no disponible"
        val textti = findViewById<TextView>(R.id.txttitulo)
        textti.text = titulo

    }
}

class DatabaseHelper3(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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

    data class Capitulo(
        val numeroCapitulo: Int,
        val titulo: String,
        val contenido: String,
        val portada: ByteArray? // BLOB para la imagen, puede ser nulo
    )

    @SuppressLint("Range")
    fun obtenerCapitulos(idHistoria: Int): MutableList<Capitulo> {
        val info = mutableListOf<Capitulo>()
        val db = this.readableDatabase

        // Realizamos una sola consulta para obtener ambos campos: numeroCapitulo y titulo
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
                Log.d("DatabaseDebug", "Tamaño del BLOB: ${portada.size}")
                Log.d("DatabaseDebug", "content: ${contenido}")
                info.add(Capitulo(numeroCapitulo, titulo, contenido, portada))
            } while (cursor.moveToNext())

        }
        Log.e("DatabaseError", "error")
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
class CapituloAdapter(private val context: Context, private val capitulos: List<DatabaseHelper3.Capitulo>) : BaseAdapter() {

    override fun getCount(): Int = capitulos.size

    override fun getItem(position: Int): DatabaseHelper3.Capitulo = capitulos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val capitulo = getItem(position)
        val view = LayoutInflater.from(context).inflate(R.layout.redaccion, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.imageContent)
        val textView = view.findViewById<TextView>(R.id.txtcont)

        if (capitulo.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(capitulo.portada, 0, capitulo.portada.size)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }

        textView.text = "${capitulo.titulo}\n${capitulo.contenido}"


        return view
    }

}
