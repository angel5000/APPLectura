package com.example.applectura.Lectura

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.applectura.R
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class PrincipalActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var dbHelper: DatabaseHelper2
    private lateinit var historias: List<Historia>
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: PrincipalActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        gridView = findViewById(R.id.gridView)
        dbHelper = DatabaseHelper2(this)

        // Obtener las historias con portadas
        historias = dbHelper.obtenerHistoriasConPortada()

        // Crear y establecer el adaptador
        val adapter = HistoriaAdapter(this, historias)
        gridView.adapter = adapter

        // Inicializa la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializa el DrawerLayout
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        // Configura el botón de la hamburguesa
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
                    // Ir a la actividad "Slideshow"
                   // startActivity(Intent(this, SlideshowActivity::class.java))
                }
            }

            // Cerrar el menú lateral después de la selección
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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
class HistoriaAdapter(private val context: Context, private val historias: List<Historia>) : BaseAdapter() {

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
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_lecturas, parent, false)

        if (convertView == null) {
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.icon)
            holder.text = view.findViewById(R.id.text)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val historia = historias[position]

        // Establecer la portada como imagen
        historia.portada?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.icon.setImageBitmap(bitmap)
        }

        holder.text.text = historia.titulo

        return view
    }

    private class ViewHolder {
        lateinit var icon: ImageView
        lateinit var text: TextView
    }
}

