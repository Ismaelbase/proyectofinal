package com.example.tiendav1

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KFunction1

class Admin_editar_articulo : AppCompatActivity() {

    val validadores: Map<EditText, KFunction1<EditText, Boolean>> by lazy {
        mapOf(
            nombre to this::validarNombre,
            stock to this::validarStock,
            precio to this::validarPrecio,
        )
    }

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val id_articulo: String by lazy {
        intent.getStringExtra("ID")!!
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.editar_articulo_volver)
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.editar_articulo_imagen)
    }
    val nombre: EditText by lazy {
        findViewById(R.id.editar_articulo_nombre)
    }
    val precio: EditText by lazy {
        findViewById(R.id.editar_articulo_precio)
    }
    val descripcion: EditText by lazy {
        findViewById(R.id.editar_articulo_descripcion)
    }
    val stock: EditText by lazy {
        findViewById(R.id.editar_articulo_stock)
    }
    val spinner_categoria: Spinner by lazy {
        findViewById(R.id.editar_articulo_spinner)
    }
    val boton_aplicar: Button by lazy {
        findViewById(R.id.editar_articulo_aplicar)
    }
    val disponible:Switch by lazy {
        findViewById(R.id.editar_articulo_disponible)
    }

    private lateinit var pojo_articulo: Articulo
    private var url_avatar: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_articulo)

        supportActionBar?.hide()

        val adaptador_spinner = ArrayAdapter(
            applicationContext,
            R.layout.custom_spinner,
            Articulo.categorias
        )
        adaptador_spinner.setDropDownViewResource(R.layout.custom_spinner)
        spinner_categoria.adapter = adaptador_spinner

        Utilidades.articulos.child(id_articulo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_articulo = snapshot.getValue(Articulo::class.java)!!

                    nombre.setText(pojo_articulo.nombre)
                    precio.setText(pojo_articulo.precio.toString())
                    descripcion.setText(pojo_articulo.descripcion)
                    stock.setText(pojo_articulo.stock.toString())

                    disponible.isChecked = pojo_articulo.disponible!!

                    val elegido = Articulo.categorias.indexOf(pojo_articulo.categoria)
                    spinner_categoria.setSelection(elegido)

                    Glide.with(applicationContext)
                        .load(pojo_articulo.url_foto)
                        .into(imagen)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        imagen.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        imagen.setOnLongClickListener {
            val fichero_temporal = crearFicheroImagen()
            url_avatar = FileProvider.getUriForFile(
                applicationContext,
                "com.example.tiendav1.fileprovider",
                fichero_temporal
            )
            getCamara.launch(url_avatar)
            true
        }


        boton_aplicar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (validarTodo()) {
                    if (Utilidades.existeArticulo(nombre.text.toString()) &&
                        pojo_articulo.nombre.toString().trim() != nombre.text.toString().trim()) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_editar_articulo,
                            applicationContext,
                            "Ya existe un articulo con ese nombre"
                        )
                    } else {

                        val nuevo_nombre = nombre.text.toString()
                        val nuevo_precio = precio.text.toString().toDouble()
                        val nueva_descripcion = descripcion.text.toString()
                        val nuevo_stock = stock.text.toString().toInt()
                        val nueva_categoria = spinner_categoria.selectedItem.toString()
                        val url_firebase :String?

                        if (url_avatar == null) {
                            url_firebase = pojo_articulo.url_foto
                        } else {
                            url_firebase = Utilidades.guardarImagen(
                                referencia_almacenamiento,
                                pojo_articulo.id!!,
                                url_avatar!!
                            )
                        }

                        Utilidades.escribirArticulo(
                        id_articulo,
                        nuevo_nombre,
                        nuevo_precio,
                        nueva_descripcion,
                        nueva_categoria,
                        url_firebase!!,
                        pojo_articulo.fecha!!,
                        nuevo_stock,
                        disponible.isChecked,
                        Estado.MODIFICADO,
                        Utilidades.obtenerIDUsuario(applicationContext))

                        Utilidades.tostadaCorrutina(
                            this@Admin_editar_articulo,
                            applicationContext,
                            "Articulo editado correctamente"
                        )

                        finish()

                    }
                }
            }

        }

        boton_volver.setOnClickListener {
            val intent = Intent(applicationContext, Admin_principal::class.java)
            Utilidades.admin_editar = true
            startActivity(intent)
        }

    }

    fun validarTodo(): Boolean {
        var correcto = true
        validadores.forEach { entrada, funcion ->
            correcto = correcto && funcion(entrada)
        }
        return correcto
    }

    fun validarNombre(e: EditText): Boolean {
        var correcto: Boolean = false

        var nombre = e.text.toString()
        if (nombre.length > 0) {
            correcto = true
        }else{
            runOnUiThread {
                e.error = "El nombre no puede estar vacio"
            }

        }

        return correcto
    }

    fun validarStock(e: EditText): Boolean {
        var correcto: Boolean = false

        var stock = e.text.toString().toIntOrNull()
        if (stock != null) {
            if (stock >= 0) {
                correcto = true
            }else{
                runOnUiThread {
                    e.error = "El stock no puede ser negativo"
                }
            }
        }else{
            runOnUiThread {
                e.error = "El stock no puede estar vacio"
            }
        }

        return correcto
    }

    fun validarPrecio(e: EditText): Boolean {
        var correcto: Boolean = false

        var precio = e.text.toString().toDoubleOrNull()
        if (precio != null) {
            if (precio >=0) {
                correcto = true
            }else{
                runOnUiThread {
                    e.error = "El precio no puede ser negativo"
                }
            }
        }else{
            runOnUiThread {
                e.error = "El precio no puede estar vacio"
            }
        }

        return correcto
    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_avatar = uri
            imagen.setImageURI(url_avatar)
        }
    }

    val getCamara = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            imagen.setImageURI(url_avatar)
        } else {
            Toast.makeText(
                applicationContext,
                "No has hecho ninguna foto",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun crearFicheroImagen(): File {
        val cal: Calendar? = Calendar.getInstance()
        val timeStamp: String? = SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
        val nombreFichero: String? = "JPGE_" + timeStamp + "_"
        val carpetaDir: File? = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val ficheroImagen: File? = File.createTempFile(nombreFichero!!, ".jpg", carpetaDir)

        return ficheroImagen!!
    }
}