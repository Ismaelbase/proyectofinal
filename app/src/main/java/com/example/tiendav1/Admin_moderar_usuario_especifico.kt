package com.example.tiendav1

import android.media.Image
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

class Admin_moderar_usuario_especifico : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.moderar_usuario_imagen)
    }
    val contrasena: EditText by lazy {
        findViewById(R.id.moderar_usuario_contrasena)
    }
    val alta: Switch by lazy {
        findViewById(R.id.moderar_usuario_alta)
    }
    val nombre: TextView by lazy {
        findViewById(R.id.moderar_usuario_tv_nombre)
    }
    val aplicar: Button by lazy {
        findViewById(R.id.moderar_usuario_boton_aplicar)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.moderar_usuario_boton_volver)
    }

    private lateinit var pojo_usuario: User
    private var url_avatar: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_moderar_usuario_especifico)

        supportActionBar!!.hide()
        val id_usuario = intent.getStringExtra("ID")

        Utilidades.usuarios.child(id_usuario!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_usuario = snapshot.getValue(User::class.java)!!

                    nombre.text = pojo_usuario.usuario
                    contrasena.setText(pojo_usuario.contraseña)
                    alta.isChecked = pojo_usuario.alta!!

                    Glide.with(applicationContext)
                        .load(pojo_usuario.url_avatar)
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

        var correcto = false
        aplicar.setOnClickListener {
            runOnUiThread {
                correcto = validarContraseña(contrasena)
            }
            GlobalScope.launch(Dispatchers.IO) {
                if (correcto) {
                    val url_firebase: String?

                    if (url_avatar == null) {
                        url_firebase = pojo_usuario.url_avatar
                    } else {

                        url_firebase = Utilidades.guardarImagen(
                            referencia_almacenamiento,
                            pojo_usuario.id.toString(),
                            url_avatar!!
                        )
                    }

                    Utilidades.escribirUser(
                        referencia_bd,
                        pojo_usuario.id.toString(),
                        pojo_usuario.usuario.toString(),
                        contrasena.text.toString(),
                        pojo_usuario.correo.toString(),
                        pojo_usuario.puntos!!,
                        url_firebase.toString(),
                        pojo_usuario.fecha.toString(),
                        alta.isChecked!!,
                        pojo_usuario.conectado!!,
                        pojo_usuario.admin!!
                    )


                    runOnUiThread {
                      Toast.makeText(
                          application,
                            "Cambios realizados con éxito",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    //si no hago esto no me deja cambiar nada mas despues de cambiar el nombre
                    // porque dice que ya existe el usuario
                    finish()

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

    fun validarContraseña(e: EditText): Boolean {
        var correcto: Boolean
        var valor = e.text.toString().trim()

        if (valor.isEmpty()) {
            correcto = false
            e.error = "La contraseña debe ser de 8 a 16 carácteres de largo y tener: \n\n" +
                    "   \t - Una letra mayúscula\n" +
                    "   \t - Una letra minúscula\n" +
                    "   \t - Un número\n" +
                    "   \t - Un símbolo\n" +
                    "   \t - No tener espacios"
        } else if (valor.matches("^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[^\\w\\d\\s:])([^\\s]){8,16}\$".toRegex())
        ) {
            correcto = true
        } else {
            correcto = false
            e.error = "La contraseña debe ser de 8 a 16 carácteres de largo y tener: \n" +
                    "   \t - Una letra mayúscula\n" +
                    "   \t - Una letra minúscula\n" +
                    "   \t - Un número\n" +
                    "   \t - Un símbolo"
        }

        return correcto
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