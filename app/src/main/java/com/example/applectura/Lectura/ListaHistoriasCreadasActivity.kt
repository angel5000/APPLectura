package com.example.applectura.Lectura

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ListaHistoriasCreadasActivity : AppCompatActivity() {
    private lateinit var dbHelper: Mostrarhistdb
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_historia)
        val btfloat = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
       // gridView = findViewById(R.id.gridhistcreada)
        dbHelper = Mostrarhistdb(this)

        val idUsuario = 1 // Reemplaza con el ID del usuario actual
        val historias = dbHelper.obtenerHistoriasPorUsuario(idUsuario)

        recyclerView = findViewById(R.id.gridhistcreada)
        recyclerView.layoutManager = GridLayoutManager(this, 1) // 2 columnas como el GridView
        recyclerView.adapter = ListHistoriaadapter(this, historias)




// Set Dashboard selected
        bottomNavigationView.selectedItemId = R.id.navigation_agregar

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

            btfloat.setOnClickListener {
                val intent = Intent(this, CreaHistoriaActivity::class.java)
                startActivity(intent)

            }


    }
}
data class HistoriaCreada(val idHistoria: Int, val titulo: String, val portada: ByteArray?, val fecha: String, val usuarionomb:String)
class Mostrarhistdb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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


    @SuppressLint("Range")
    fun obtenerHistoriasPorUsuario(idUsuario: Int): MutableList<HistoriaCreada> {
        val historiascr = mutableListOf<HistoriaCreada>()
        val db = this.readableDatabase
        val query = """
    SELECT Historia.idHistoria, Historia.titulo, Historia.portada, Historia.fechaPublicacion, Usuario.nombreUsuario
    FROM Historia 
    JOIN Usuario ON Historia.idUsuario = Usuario.idUsuario 
    WHERE Historia.idUsuario = ?
""".trimIndent()
        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))

        if (cursor.moveToFirst()) {
            do {
                val idHistoria = cursor.getInt(cursor.getColumnIndex("idHistoria"))
                val titulo = cursor.getString(cursor.getColumnIndex("titulo"))
                val portadaBytes = cursor.getBlob(cursor.getColumnIndex("portada"))
                val fecha = cursor.getString(cursor.getColumnIndex("fechaPublicacion"))
                val usrnombre = cursor.getString(cursor.getColumnIndex("nombreUsuario"))

                historiascr.add(HistoriaCreada(idHistoria, titulo, portadaBytes,fecha,usrnombre))
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

class ListHistoriaadapter( private val context: Context,
                           private val historias: MutableList<HistoriaCreada>
) : RecyclerView.Adapter<ListHistoriaadapter.HistoriaViewHolder>() {

    private val db = Mostrarhistdb(context)

    inner class HistoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val text: TextView = itemView.findViewById(R.id.txtnombhist)
        val fecha: TextView = itemView.findViewById(R.id.txfecha)
        val autor: TextView = itemView.findViewById(R.id.txtautor)
        val btnEliminar: Button = itemView.findViewById(R.id.bteliminarhist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.items_creacion_historia, parent, false)
        return HistoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoriaViewHolder, position: Int) {
        val historia = historias[position]

        // Establecer la portada
        if (historia.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(historia.portada, 0, historia.portada.size)
            holder.icon.setImageBitmap(bitmap)
        } else {
            holder.icon.setImageBitmap(null) // Imagen predeterminada si no hay portada
        }

        holder.text.text = historia.titulo
        holder.fecha.text = historia.fecha
        holder.autor.text = historia.usuarionomb

        // Acción del botón Eliminar
        holder.btnEliminar.setOnClickListener {
            val success = db.eliminarHistoria(historia.idHistoria)

            if (success) {
                historias.removeAt(position)
                notifyItemRemoved(position)
            } else {
                Toast.makeText(context, "No se pudo eliminar la historia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return historias.size
    }
}
