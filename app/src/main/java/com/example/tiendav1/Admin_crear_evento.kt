package com.example.tiendav1

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.util.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class Admin_crear_evento : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.crear_evento_imagen)
    }
    val nombre: EditText by lazy {
        findViewById(R.id.crear_evento_nombre)
    }
    val precio: EditText by lazy {
        findViewById(R.id.crear_evento_precio)
    }
    val aforo: EditText by lazy {
        findViewById(R.id.crear_evento_aforo)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.crear_evento_boton_volver)
    }
    val boton_crear: Button by lazy {
        findViewById(R.id.crear_evento_boton_crear)
    }

    private var url_avatar: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_crear_evento)

        supportActionBar?.hide()

        imagen.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        boton_crear.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (nombre.text.isNotEmpty() && precio.text.isNotEmpty() && aforo.text.isNotEmpty()) {

                    if (Utilidades.existeevento(referencia_bd, nombre.text.toString().trim())) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "Ya existe un evento con ese nombre"
                        )
                    } else if (precio.text.toString().toInt() < 0) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "El precio no puede ser negativo"
                        )
                    } else if (aforo.text.toString().toInt() < 0) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "El aforo no puede ser negativo"
                        )
                    } else if (url_avatar == null) {

                        imagen.setImageResource(R.drawable.anadir_imagen_symbol_error)

                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                imagen.setImageResource(R.drawable.anadir_imagen_symbol_sc)
                            }
                        }, 3000)
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "Debes añadir una imagen"
                        )
                    } else {

                        val fecha = LocalDate.now().toString()
                        val id_evento = Utilidades.eventos.push().key!!
                        GlobalScope.launch(Dispatchers.IO) {


                            val url_firebase = Utilidades.guardarImagen(
                                referencia_almacenamiento,
                                id_evento,
                                url_avatar!!
                            )

                            Utilidades.escribirEvento(
                                referencia_bd,
                                id_evento,
                                nombre.text.toString().trim(),
                                fecha,
                                precio.text.toString().toDouble(),
                                aforo.text.toString().toInt(),
                                url_firebase
                            )

                            Utilidades.tostadaCorrutina(
                                this@Admin_crear_evento,
                                applicationContext,
                                "Evento creado correctamente"
                            )
                            finish()
                        }
                    }
                }else {
                    Utilidades.tostadaCorrutina(
                        this@Admin_crear_evento,
                        applicationContext,
                        "Rellena todos los campos"
                    )
                }
            }
        }










        boton_volver.setOnClickListener {
            finish()
        }


    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_avatar = uri
            imagen.setImageURI(url_avatar)
        }
    }
}