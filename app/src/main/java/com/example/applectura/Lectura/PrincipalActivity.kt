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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.applectura.R
import com.google.android.material.bottomnavigation.BottomNavigationView
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


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

// Set Home selected
        bottomNavigationView.selectedItemId = R.id.navigation_principal

// Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_agregar -> {
                    val intent = Intent(applicationContext,CrearHistoriaActivity  ::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_principal -> true
                R.id.navigation_principal -> {
                    val intent = Intent(applicationContext, PrincipalActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }







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

        gridView.setOnItemClickListener { parent, view, position, id ->
            val tag = view.tag as Pair<RecyclerView.ViewHolder, Int> // Obtener el Pair (ViewHolder, ID)
            val historiaId = tag.second // Aquí tienes el ID de la historia

            // Enviar el ID a la otra actividad
            val intent = Intent(this, LecturaActivity::class.java)
            intent.putExtra("ITEM_ID", historiaId)
            startActivity(intent)
        }
/*
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (savedInstanceState == null) {
            loadFragment(PrincipalFragment()) // Solo carga el fragmento inicial si no hay estado guardado
            bottomNavigation.selectedItemId = R.id.navigation_principal // Marca la opción seleccionada por defecto
        }
        // Configurar las acciones para cada opción
       bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_principal -> {
                    loadFragment(PrincipalFragment())
                    true
                }
                R.id.navigation_agregar -> {
                    loadFragment(CrearFragment())
                    true
                }
                /* R.id.navigation_buscar -> {
                     loadFragment(BuscarFragment())
                     true
                 }*/
                else -> false
            }
        }

*/





    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.navigation_principal, fragment).commit()
    }/*
    private fun loadFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment?.javaClass == fragment.javaClass) {
            return // Si el fragmento es el mismo, no lo reemplaces
        }

        // Reemplaza el fragmento en el contenedor
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }*/
    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        return drawerLayout.isDrawerOpen(GravityCompat.START) || super.onSupportNavigateUp()
    }




}

class PrincipalFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_principal, container, false)
    }
}
class CrearFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_crear_historia, container, false)
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
            view.tag = Pair(holder, historias[position].idHistoria)
        } else {
            val tag = view.tag as Pair<ViewHolder, Int>
            holder = tag.first
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

