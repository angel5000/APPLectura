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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class BuscarHistoriaActivity : AppCompatActivity() {
    private lateinit var dbHelper: Buscarhistdb
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextBuscar: EditText
    private lateinit var btBuscarHistoria: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_historia)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_buscar
        editTextBuscar = findViewById(R.id.editTextBuscar)
        recyclerView = findViewById(R.id.gridhistbuscar)
        btBuscarHistoria = findViewById(R.id.btbuscarhistoria)
        dbHelper = Buscarhistdb(this)


        btBuscarHistoria.setOnClickListener {
            val nombre = editTextBuscar.text.toString()

            // Obtener las historias desde la base de datos
            val HistoriaCreada = dbHelper.Buscarpornombre(nombre)

            // Configurar el RecyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.gridhistbuscar)


            recyclerView.layoutManager = GridLayoutManager(this, 1)
            val adapter = BuscaHistoriaadapter(this,
            HistoriaCreada
            ) { historiaId ->
                // Acciones cuando se hace clic en un elemento
                Toast.makeText(this, "id: $historiaId", Toast.LENGTH_SHORT).show()

                // Enviar el ID a la otra actividad
                val intent = Intent(this, LecturaActivity::class.java)
                intent.putExtra("ITEM_ID", historiaId)
                startActivity(intent)
            }
            recyclerView.adapter = adapter
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
            "LecturaAPPBD.db"
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





    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}

class BuscaHistoriaadapter(
    private val context: Context,
    private val historias: MutableList<HistoriaBuscar>,
    private val itemClickListener: (Int) -> Unit
) : RecyclerView.Adapter<BuscaHistoriaadapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.grid_busca_historia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historia = historias[position]

        // Establecer los datos del elemento
        if (historia.portada != null) {
            val bitmap = BitmapFactory.decodeByteArray(historia.portada, 0, historia.portada.size)
            holder.icon.setImageBitmap(bitmap)
        } else {
            holder.icon.setImageBitmap(null)
        }

        holder.text.text = historia.titulo
        holder.fecha.text = historia.fecha
        holder.autor.text = historia.usuarionomb

        // Asignar un clic al item
        holder.itemView.setOnClickListener {
            itemClickListener(historia.idHistoria)
        }
    }

    override fun getItemCount(): Int = historias.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val text: TextView = view.findViewById(R.id.txtnombhist)
        val fecha: TextView = view.findViewById(R.id.txfecha)
        val autor: TextView = view.findViewById(R.id.txtautor)
    }

}
