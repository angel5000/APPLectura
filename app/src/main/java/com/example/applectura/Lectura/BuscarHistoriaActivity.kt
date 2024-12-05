package com.example.applectura.Lectura

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class BuscarHistoriaActivity : AppCompatActivity() {
    private lateinit var dbHelper: Buscarhistdb
    private lateinit var gridView: GridView
    private lateinit var editTextBuscar: EditText
    private lateinit var btBuscarHistoria: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_historia)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_buscar
        editTextBuscar = findViewById(R.id.editTextBuscar)
        gridView = findViewById(R.id.gridhistbuscar)
        btBuscarHistoria = findViewById(R.id.btbuscarhistoria)
        dbHelper = Buscarhistdb(this)

        val idUsuario = 1 // Reemplaza con el ID del usuario actual
        btBuscarHistoria.setOnClickListener {
            val nombre = editTextBuscar.text.toString()
            val HistoriaCreada = dbHelper.Buscarpornombre( nombre)
            val adapter = BuscaHistoriaadapter(this, HistoriaCreada)
            gridView.adapter = adapter
        }




// Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_principal -> {
                    val intent = Intent(applicationContext, PrincipalActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.navigation_agregar -> {
                    val intent = Intent(applicationContext, ListaHistoriasCreadasActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.navigation_buscar -> {
                    val intent = Intent(applicationContext, BuscarHistoriaActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}

data class HistoriaBuscar(val idHistoria: Int, val titulo: String, val portada: ByteArray?, val fecha: String, val usuarionomb:String)
class Buscarhistdb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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


    fun Buscarpornombre(nombre: String): MutableList<HistoriaBuscar> {
        val historiascr = mutableListOf<HistoriaBuscar>()
        val db = this.readableDatabase

        val query = """
        SELECT Historia.idHistoria, Historia.titulo, Historia.portada, Historia.fechaPublicacion, Usuario.nombreUsuario
    FROM Historia 
    JOIN Usuario ON Historia.idUsuario = Usuario.idUsuario 
    WHERE titulo LIKE ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf("%$nombre%"))

        if (cursor.moveToFirst()) {
            do {
                val idHistoria = cursor.getInt(cursor.getColumnIndexOrThrow("idHistoria"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val portadaBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("portada"))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fechaPublicacion"))
                val usrnombre = cursor.getString(cursor.getColumnIndexOrThrow("nombreUsuario"))

                historiascr.add(HistoriaBuscar(idHistoria, titulo, portadaBytes, fecha, usrnombre))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historiascr
    }

    fun eliminarHistoria(idHistoria: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("Historia", "idHistoria = ?", arrayOf(idHistoria.toString()))
        return result > 0
    }



    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}

class  BuscaHistoriaadapter(private val context: Context, private val historias: MutableList<HistoriaBuscar>) :
    BaseAdapter() {

    private val db = Mostrarhistdb(context)

    override fun getCount(): Int {
        return historias.size
    }

    override fun getItem(position: Int): Any {
        return historias[position]
    }

    override fun getItemId(position: Int): Long {
        return historias[position].idHistoria.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_busca_historia, parent, false)

        if (convertView == null) {
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.icon)
            holder.text = view.findViewById(R.id.txtnombhist)
            holder.fecha = view.findViewById(R.id.txfecha)
            holder.autor = view.findViewById(R.id.txtautor)


            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val historia = historias[position]

        // Establecer la portada
        if (historia.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(historia.portada, 0, historia.portada.size)
            holder.icon.setImageBitmap(bitmap)
        } else {
            // Imagen predeterminada si no hay portada
            holder.icon.setImageBitmap(null)
        }

        holder.text.text = historia.titulo
        holder.fecha.text = historia.fecha
        holder.autor.text = historia.usuarionomb

        // Acción del botón Eliminar


        return view
    }

    // ViewHolder para optimizar las vistas
    private class ViewHolder {
        lateinit var icon: ImageView
        lateinit var text: TextView
        lateinit var fecha: TextView
        lateinit var autor: TextView

    }
}
