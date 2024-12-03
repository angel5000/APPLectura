package com.example.applectura.Lectura

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.applectura.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import jp.wasabeef.blurry.Blurry

class LecturaActivity : AppCompatActivity() {
    var iddatos=1
    var datos=""
    private lateinit var btleer: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lectura2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val textView = findViewById<TextView>(R.id.textdescrip)
        textView.movementMethod = ScrollingMovementMethod()
        val itemId = intent.getIntExtra("ITEM_ID", -1) // Recuperar el ID que fue enviado



        ///////////



        // Usar el ID para realizar alguna acción (por ejemplo, cargar más detalles de la historia)
        if (itemId != -1) {
            iddatos = itemId
        }

        btleer = findViewById(R.id.btleer)
        btleer.setOnClickListener {
            val intent = Intent(this, RedaccionActivity::class.java)
            intent.putExtra("ITEM_IDRED", iddatos)
            intent.putExtra("titulo", datos)
            startActivity(intent)
        }

        // Habilitar el botón de retroceso si es necesario
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val dbHelper = DatabaseHelper(this)
        val txtNombreLectura = findViewById<TextView>(R.id.txtnomblect)
        val txtDescripcionLectura = findViewById<TextView>(R.id.textdescrip)
// Obtener datos de la base de datos
        val lectura = dbHelper.obtenerTituloPorId(iddatos)
        txtNombreLectura.text = lectura

        val imageView: ImageView = findViewById(R.id.imagePortada)
        val imagefondo: ImageView = findViewById(R.id.texture2)
// Obtener la imagen desde la base de datos
        val portadaBitmap =
            dbHelper.obtenerPortada(iddatos) // Suponiendo que '1' es el id de la historia

// Verificar si la imagen existe y cargarla
        if (portadaBitmap != null) {
            imageView.setImageBitmap(portadaBitmap)
            imagefondo.setImageBitmap(portadaBitmap)
        }

        val sinopsis = dbHelper.obtenerSinopsis(iddatos)
        txtDescripcionLectura.text = sinopsis
        ////////////

        val comentarios = dbHelper.obtenerComentariosPorCapitulo(iddatos)

        val gridView: GridView = findViewById(R.id.gridcomentario)

        // Configurar el adaptador para mostrar los datos
        /* val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, // Layout predefinido para un solo elemento
            comentarios // Lista de comentarios
        )
        gridView.adapter = adapter*/
        val adapter = MyAdapter(this, comentarios)
        gridView.adapter = adapter
        val gridView2: GridView = findViewById(R.id.gridviewcap)
        val capitulos =
            dbHelper.obtenercapitulos(iddatos)
        val tituloRed =
            dbHelper.obtenercapitulosTitulo(iddatos)// Este método debería devolver la lista de capítulos
        val adapter2 = GridCap(this, capitulos)
        val adapterTitulo = GridCap(this, tituloRed)

        if (tituloRed.isNotEmpty()) {
            datos = adapterTitulo.getItem(0).toString()
        } else {
            datos=""
        }

        gridView2.adapter = adapter2
        val imageView2: ImageView = findViewById(R.id.texture2)



        imageView2.post {
            Blurry.with(this)
                .radius(10) // Intensidad del desenfoque
                .capture(imageView2) // Captura el contenido visible del ImageView
                .into(imageView2) // Reemplaza el contenido desenfocado en el ImageView
        }



    }


/////////



    override fun onSupportNavigateUp(): Boolean {

        finish() // Finaliza la actividad actual para que no quede en la pila
        return true
    }

}
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    fun obtenerTituloPorId(id: Int): String {
        val db = this.readableDatabase
        val query = "SELECT Titulo FROM Historia WHERE idHistoria = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
            cursor.close()
            titulo
        } else {
            cursor.close()
            "No encontrado"
        }
    }
    fun obtenerSinopsis(idHistoria: Int): String? {
        val db = this.readableDatabase
        val query = "SELECT sinopsis FROM Historia WHERE idHistoria = ?"
        var sinopsis: String? = null

        val cursor = db.rawQuery(query, arrayOf(idHistoria.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex("sinopsis")
                if (columnIndex != -1) {
                    sinopsis = it.getString(columnIndex)
                } else {
                    Log.e("DatabaseError", "La columna 'sinopsis' no existe.")
                }
            } else {
                Log.e("DatabaseError", "No se encontró el registro con idHistoria = $idHistoria.")
            }
        }

        db.close()
        return sinopsis
    }
    fun obtenerPortada(idHistoria: Int): Bitmap? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT portada FROM Historia WHERE idHistoria = ?", arrayOf(idHistoria.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                val columnaPortadaIndex = it.getColumnIndex("portada")
                Log.d("DatabaseDebug", "Índice de la columna 'portada': $columnaPortadaIndex")

                if (columnaPortadaIndex != -1) {
                    val portadaBlob = it.getBlob(columnaPortadaIndex)
                    if (portadaBlob != null && portadaBlob.isNotEmpty()) {
                        Log.d("DatabaseDebug", "Tamaño del BLOB: ${portadaBlob.size}")
                        return BitmapFactory.decodeByteArray(portadaBlob, 0, portadaBlob.size)
                    } else {
                        Log.e("DatabaseError", "El BLOB está vacío o es NULL.")
                    }
                } else {
                    Log.e("DatabaseError", "La columna 'portada' no existe en el resultado.")
                }
            } else {
                Log.e("DatabaseError", "No se encontró la fila para idHistoria = $idHistoria.")
            }
        }

        return null
    }



    fun obtenerComentariosPorCapitulo(idHistoria: Int): List<String> {
        val db = this.readableDatabase
        val comentarios = mutableListOf<String>()
        val query = "SELECT contenido FROM Comentario WHERE idHistoria = ?"

        val cursor = db.rawQuery(query, arrayOf(idHistoria.toString()))
        cursor.use {
            while (it.moveToNext()) {
                val columnIndex = it.getColumnIndex("contenido")
                if (columnIndex != -1) {
                    comentarios.add(it.getString(columnIndex))
                } else {
                    Log.e("DatabaseError", "La columna 'contenido' no existe.")
                }
            }
        }

        db.close()
        return comentarios
    }

    @SuppressLint("Range")
    fun obtenercapitulos(idCAP: Int): List<String> {
        val db = this.readableDatabase
        val comentarios = mutableListOf<String>()
        // Realizamos una sola consulta para obtener ambos campos: numeroCapitulo y titulo
        val query = "SELECT numeroCapitulo, titulo FROM Capitulo WHERE idHistoria = ?"

        val cursor = db.rawQuery(query, arrayOf(idCAP.toString()))
        cursor.use {
            // Recorremos el cursor para obtener los resultados
            while (it.moveToNext()) {
                // Obtener los valores de las columnas
                val numeroCapitulo = it.getInt(it.getColumnIndex("numeroCapitulo"))
                val titulo = it.getString(it.getColumnIndex("titulo"))

                // Añadir el número de capítulo y título a la lista en el formato deseado
                comentarios.add("Capítulo $numeroCapitulo: $titulo")
            }
        }

        db.close()
        return comentarios
    }
    @SuppressLint("Range")
    fun obtenercapitulosTitulo(idCAP: Int): List<String> {
        val db = this.readableDatabase
        val comentarios = mutableListOf<String>()
        // Realizamos una sola consulta para obtener ambos campos: numeroCapitulo y titulo
        val query = "SELECT numeroCapitulo, titulo FROM Capitulo WHERE idHistoria = ? LIMIT 1"

        val cursor = db.rawQuery(query, arrayOf(idCAP.toString()))
        cursor.use {
            // Recorremos el cursor para obtener los resultados
            while (it.moveToNext()) {
                // Obtener los valores de las columnas
                val numeroCapitulo = it.getInt(it.getColumnIndex("numeroCapitulo"))
                val titulo = it.getString(it.getColumnIndex("titulo"))

                // Añadir el número de capítulo y título a la lista en el formato deseado
                comentarios.add("Capítulo $numeroCapitulo\n$titulo")
            }
        }

        db.close()
        return comentarios
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}
class GridCap(
    private val context: Context,
    private val dataList: List<String> // Lista de capítulos con numero y titulo
) : BaseAdapter() {

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder


        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.grid_item_capitulos, parent, false)

            holder = ViewHolder()
            holder.textViewNumero = view.findViewById(R.id.textViewNumero)
            holder.textViewTitulo = view.findViewById(R.id.textViewTitulo)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        // Obtener el capitulo en formato "Capítulo X: Título"
        val capitulo = dataList[position]

        // Dividir el string en número y título
        val parts = capitulo.split(": ")
        if (parts.size == 2) {
            // Asignar los valores divididos a los TextViews
            holder.textViewNumero.text = parts[0] // Número de capítulo
            holder.textViewTitulo.text = parts[1] // Título del capítulo




        }

        return view
    }

    // ViewHolder para optimizar la búsqueda de vistas
    private class ViewHolder {
        lateinit var textViewNumero: TextView
        lateinit var textViewTitulo: TextView
    }
}


    private class ViewHolder {
        lateinit var textViewNumero: TextView
        lateinit var textViewTitulo: TextView
    }






class MyAdapter(private val context: Context, private val dataList: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.grid_item_comentarios, parent, false)

            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.textView)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        // Set the item text and color
        holder.textView.text = dataList[position]
        holder.textView.setTextColor(Color.WHITE) // Set text color to white

        return view
    }

    private class ViewHolder {
        lateinit var textView: TextView
    }
}

class GridAdapter(private val context: Context, private val countries: Array<String>, private val flags: Array<Int>) :
    BaseAdapter() {

    override fun getCount(): Int = countries.size

    override fun getItem(position: Int): Any = countries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_comentarios, parent, false)


        val textView = view.findViewById<TextView>(R.id.textView)

        textView.text = countries[position]

        return view
    }
}