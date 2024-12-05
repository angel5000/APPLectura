package com.example.applectura.Lectura

import android.annotation.SuppressLint
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
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class PrincipalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper2
    private lateinit var historias: List<Historia>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_principal)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.navigation_principal
        recyclerView = findViewById(R.id.gridView)
        dbHelper = DatabaseHelper2(this)

        historias = dbHelper.obtenerHistoriasConPortada()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_agregar -> {
                    val intent = Intent(applicationContext,ListaHistoriasCreadasActivity  ::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.navigation_principal -> {
                    val intent = Intent(applicationContext, PrincipalActivity::class.java)
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



        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )




        // Activa el botón de la hamburguesa
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Inicializa el NavigationView
        val navView: NavigationView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Verificar si ya estamos en la actividad PrincipalActivity
                    if (javaClass != PrincipalActivity::class.java) {
                        // Si no estamos en PrincipalActivity, abrirla
                        startActivity(Intent(this, PrincipalActivity::class.java))
                    }
                }
                R.id.nav_Perfil -> {

                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                R.id.nav_slideshow -> {

                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }



        recyclerView.layoutManager = GridLayoutManager(this, 2)

// Crear el adaptador con la lista y el callback para clics
        val adapter = HistoriaAdapter(historias) { historiaId ->
            // Manejar el clic en un elemento
            Toast.makeText(this, "ID de historia: $historiaId", Toast.LENGTH_SHORT).show()
            // Enviar el ID a otra actividad
            val intent = Intent(this, LecturaActivity::class.java)
            intent.putExtra("ITEM_ID", historiaId)
            startActivity(intent)
        }
// Asignar el adaptador al RecyclerView
        recyclerView.adapter = adapter




    }

    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        return drawerLayout.isDrawerOpen(GravityCompat.START) || super.onSupportNavigateUp()
    }




}



data class Historia(val idHistoria: Int, val titulo: String, val portada: ByteArray?)
class DatabaseHelper2(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    fun obtenerHistoriasConPortada(): List<Historia> {
        val historias = mutableListOf<Historia>()
        val db = this.readableDatabase
        val query = "SELECT idHistoria, titulo, portada FROM Historia"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val idHistoria = cursor.getInt(cursor.getColumnIndex("idHistoria"))
                val titulo = cursor.getString(cursor.getColumnIndex("titulo"))
                val portadaBytes = cursor.getBlob(cursor.getColumnIndex("portada"))

                historias.add(Historia(idHistoria, titulo, portadaBytes))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historias
    }
    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


}
class HistoriaAdapter(
    private val historias: List<Historia>,
    private val itemClickListener: (Int) -> Unit // Callback para manejar clics
) : RecyclerView.Adapter<HistoriaAdapter.HistoriaViewHolder>() {

    class HistoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val text: TextView = itemView.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_lecturas, parent, false)
        return HistoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoriaViewHolder, position: Int) {
        val historia = historias[position]

        // Configurar imagen
        historia.portada?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.icon.setImageBitmap(bitmap)
        }

        // Configurar texto
        holder.text.text = historia.titulo

        // Configurar clic
        holder.itemView.setOnClickListener {
            itemClickListener(historia.idHistoria) // Pasar idHistoria al callback
        }
    }

    override fun getItemCount(): Int {
        return historias.size
    }
}