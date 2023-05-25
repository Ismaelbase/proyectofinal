package com.example.tiendav1

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KFunction1

class Admin_anadir_articulo : AppCompatActivity() {

    val validadores: Map<EditText, KFunction1<EditText, Boolean>> by lazy {
        mapOf(
            nombre to this::validarNombre,
            stock to this::validarStock,
            precio to this::validarPrecio,
        )
    }

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.anadir_articulo_imagen)
    }
    val nombre: EditText by lazy {
        findViewById(R.id.anadir_articulo_nombre)
    }
    val precio: EditText by lazy {
        findViewById(R.id.anadir_articulo_precio)
    }
    val spinner_categoria: Spinner by lazy {
        findViewById(R.id.anadir_articulo_spinner)
    }
    val stock: EditText by lazy {
        findViewById(R.id.anadir_articulo_stock)
    }
    val descripcion: EditText by lazy {
        findViewById(R.id.anadir_articulo_descripcion)
    }
    val boton_anadir: Button by lazy {
        findViewById(R.id.anadir_articulo_boton)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.anadir_articulo_volver)
    }
    val disponible:Switch by lazy {
        findViewById(R.id.anadir_articulo_disponible)
    }

    private var url_avatar: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_anadir_articulo)

        supportActionBar?.hide()

        disponible.isChecked = true


        val adaptador_spinner = ArrayAdapter(
            applicationContext,
            R.layout.custom_spinner,
            Articulo.categorias
        )
        adaptador_spinner.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner_categoria.adapter = adaptador_spinner

        imagen.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        boton_anadir.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (validarTodo()) {
                    if (!Utilidades.existeArticulo(nombre.text.toString())) {
                        if (url_avatar != null) {
                            val id_articulo = Utilidades.articulos.push().key!!
                            val fecha = LocalDate.now().toString()

                            val url_firebase = Utilidades.guardarImagen(
                                referencia_almacenamiento,
                                id_articulo,
                                url_avatar!!
                            )

                            Utilidades.escribirArticulo(
                                referencia_bd,
                                id_articulo,
                                nombre.text.toString(),
                                precio.text.toString().toDouble(),
                                descripcion.text.toString(),
                                spinner_categoria.selectedItem.toString(),
                                url_firebase,
                                fecha,
                                stock.text.toString().toInt(),
                                disponible.isChecked
                            )

                            Utilidades.admin_anadir = true
                            startActivity(Intent(applicationContext, Admin_principal::class.java))

                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Articulo añadido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }else{
                            imagen.setImageResource(R.drawable.anadir_imagen_symbol_error)

                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    imagen.setImageResource(R.drawable.anadir_imagen_symbol_sc)
                                }
                            }, 3000)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Ya existe un articulo con ese nombre",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
            }

        }

        boton_volver.setOnClickListener {
            Utilidades.admin_anadir = true
            startActivity(Intent(applicationContext, Admin_principal::class.java))
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
        } else {
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
            } else {
                runOnUiThread {
                    e.error = "El stock no puede ser negativo"
                }
            }
        } else {
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
            if (precio >= 0) {
                correcto = true
            } else {
                runOnUiThread {
                    e.error = "El precio no puede ser negativo"
                }
            }
        } else {
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
}