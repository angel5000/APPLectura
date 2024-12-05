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
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ListaHistoriasCreadasActivity : AppCompatActivity() {
    private lateinit var dbHelper: Mostrarhistdb
    private lateinit var gridView: GridView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_historia)
        val btfloat = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        gridView = findViewById(R.id.gridhistcreada)
        dbHelper = Mostrarhistdb(this)

        val idUsuario = 1 // Reemplaza con el ID del usuario actual
        val HistoriaCreada = dbHelper.obtenerHistoriasPorUsuario(idUsuario)

        val adapter = ListHistoriaadapter(this, HistoriaCreada)
        gridView.adapter = adapter





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
data class HistoriaCreada(val idHistoria: Int, val titulo: String, val portada: ByteArray?, val fecha: String, val usuarionomb:String)
class Mostrarhistdb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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

class ListHistoriaadapter(private val context: Context, private val historias: MutableList<HistoriaCreada>) :
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
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.items_creacion_historia, parent, false)

        if (convertView == null) {
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.icon)
            holder.text = view.findViewById(R.id.txtnombhist)
            holder.fecha = view.findViewById(R.id.txfecha)
            holder.autor = view.findViewById(R.id.txtautor)
            holder.btnEliminar = view.findViewById(R.id.bteliminarhist)

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
        holder.btnEliminar.setOnClickListener {
            // Eliminar de la base de datos
            val success = db.eliminarHistoria(historia.idHistoria)

            if (success) {
                // Eliminar de la lista y notificar al adaptador
                historias.removeAt(position)
                notifyDataSetChanged()
            } else {
                Toast.makeText(context, "No se pudo eliminar la historia", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // ViewHolder para optimizar las vistas
    private class ViewHolder {
        lateinit var icon: ImageView
        lateinit var text: TextView
        lateinit var fecha: TextView
        lateinit var autor: TextView
        lateinit var btnEliminar: Button
    }
}


/*
class ListHistoriaadapter(private val context: Context, private val historias: List<HistoriaCreada>) : BaseAdapter() {


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
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.items_creacion_historia, parent, false)

        if (convertView == null) {
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.icon)
            holder.textnomb = view.findViewById(R.id.txtnombhist)

            holder.textfecha = view.findViewById(R.id.txfecha)

            holder.textautor = view.findViewById(R.id.txtautor)
            holder.eliminar = view.findViewById(R.id.bteliminarhist)
            view.tag = Pair(holder, historias[position].idHistoria)
        } else {
            val tag = view.tag as Pair<ViewHolder, Int>
            holder = tag.first
        }

        val historia = historias[position]

        // Establecer la portada como imagen
        if (historia.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(historia.portada, 0, historia.portada.size)
            holder.icon.setImageBitmap(bitmap)
        } else {
            // Imagen predeterminada si no hay portada
            holder.icon.setImageBitmap(null)
        }

        /*  historia.portada?.let {
              val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
              holder.icon.setImageBitmap(bitmap)
          }
  */
        holder.textnomb .text = historia.titulo
        holder.textfecha.text = historia.fecha
        holder.textautor.text = historia.usuarionomb
        holder.eliminar.setOnClickListener {
            eliminarHistoria(historia.idHistoria, position)
        }
        return view
    }


    private class ViewHolder {
        lateinit var icon: ImageView
        lateinit var textautor: TextView
        lateinit var textnomb: TextView
        lateinit var textfecha: TextView
        lateinit var eliminar: Button
    }
}
*/