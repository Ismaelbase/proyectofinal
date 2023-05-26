package com.example.tiendav1

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
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

class Registro : AppCompatActivity() {

    val nombre: EditText by lazy {
        findViewById(R.id.registro_nombre)
    }
    val contrasena: EditText by lazy {
        findViewById(R.id.registro_contrasena)
    }
    val contrasena2: EditText by lazy {
        findViewById(R.id.registro_contrasena2)
    }
    val mail: EditText by lazy {
        findViewById(R.id.registro_mail)
    }
    val avatar: ImageView by lazy {
        findViewById(R.id.registro_avatar)
    }
    val boton_registrarse: Button by lazy {
        findViewById(R.id.registro_boton_registrarse)
    }
    val titulo: ImageView by lazy {
        findViewById(R.id.registro_titulo)
    }

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }

    private var url_avatar: Uri? = null
    private lateinit var lista_usuarios: MutableList<User>

    val boton_volver: ImageView by lazy {
        findViewById(R.id.registro_volver)
    }

    val validadores: Map<EditText, KFunction1<EditText, Boolean>> by lazy {
        mapOf(
            nombre to this::validarUsuario,
            contrasena to this::validarContraseña,
            mail to this::validarCorreo,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Esconder la barra del menu
        supportActionBar!!.hide()

        lista_usuarios = Utilidades.obtenerListaUsers(referencia_bd)

        //Acceso a galeria.
        avatar.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        //Colores de error en texto

        contrasena2.addTextChangedListener {
            if (contrasena.text.toString() != contrasena2.text.toString()) {
                contrasena2.setTextColor(resources.getColor(R.color.rojo_error))
            } else {
                contrasena2.setTextColor(resources.getColor(R.color.black))
            }
        }

        boton_registrarse.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (validarTodo() &&
                    !Utilidades.existeUser(nombre.text.toString().trim()) &&
                    url_avatar != null
                ) { //Se puede añadir usuario a BD y devolver al main.

                    val fecha = LocalDate.now()
                    val id_usuario = Utilidades.usuarios.push().key!!

                    val url_firebase = Utilidades.guardarImagen(
                        referencia_almacenamiento,
                        id_usuario,
                        url_avatar!!
                    )

                    Utilidades.escribirUser(
                        referencia_bd,
                        id_usuario,
                        nombre.text.toString().trim(),
                        contrasena.text.toString().trim(),
                        mail.text.toString().trim(),
                        puntos = 0,
                        url_firebase,
                        fecha.toString(),
                        true,
                        true,
                        false
                    )

                    Utilidades.tostadaCorrutina(
                        this@Registro,
                        applicationContext,
                        "Bienvenido a SecondCharm."
                    )

                    Utilidades.recien_registrado = nombre.text.toString()!!
                    startActivity(Intent(applicationContext, MainActivity::class.java))


                } else if (contrasena.text.toString() != contrasena2.text.toString()) {
                    runOnUiThread {
                        contrasena2.error = "Las contraseñas no coinciden."
                    }
                } else if (url_avatar == null) {
                    avatar.setImageResource(R.drawable.anadir_imagen_symbol_error)

                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            avatar.setImageResource(R.drawable.anadir_imagen_symbol_sc)
                        }
                    }, 3000)
                }else if (Utilidades.existeUser(nombre.text.toString().trim())){
                    nombre.setTextColor(resources.getColor(R.color.rojo_error))

                    Utilidades.tostadaCorrutina(
                        this@Registro,
                        applicationContext,
                        "Nombre no disponible."
                    )
                }else{
                    Utilidades.tostadaCorrutina(this@Registro,applicationContext,"Rellena los campos")
                }
            }
        }



        boton_volver.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

    fun validarTodo(): Boolean {
        var correcto = true
        validadores.forEach { entrada, funcion ->
            correcto = correcto && funcion(entrada)
        }
        return correcto
    }

    //VALIDADOR DE CORREO

    fun validarCorreo(e: EditText): Boolean {
        var correcto = true
        var valor = e.text.toString().trim()

        if (valor.isEmpty()) {
            correcto = false

            //Hay que ejecutar lo que interactua con la UI en el hilo de la UI o peta
            runOnUiThread {
                e.error = "El email es obligatorio."
            }


            //Devuelve true si el e-mail introducido tiene el formato correcto
            //Por lo que ponemos ! ya que queremos que salte cuando NO es correcto.
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()) {
            correcto = false
            runOnUiThread {
                e.error = "El email no tiene un formato correcto."
            }

        }

        return correcto
    }

    //VALIDADOR DE CONTRASEÑA

    fun validarContraseña(e: EditText): Boolean {
        var correcto: Boolean
        var valor = e.text.toString().trim()

        if (valor.isEmpty()) {
            correcto = false
            runOnUiThread {
                e.error = "La contraseña debe ser de 8 a 16 carácteres de largo y tener: \n\n" +
                        "   \t - Una letra mayúscula\n" +
                        "   \t - Una letra minúscula\n" +
                        "   \t - Un número\n" +
                        "   \t - Un símbolo\n" +
                        "   \t - No tener espacios"
            }


//          LO QUE DEBERIA HACER LA REGEX:
//            La contraseña debe tener un numero (0-9)
//            La contraseña debe tener una letra mayúscula
//            La contraseña debe tener una letra minúscula
//            La contraseña debe tener un simbolo
//            La contraseña debe tener de 8 a 16 carácteres
//            La contraseña no puede tener espacios
        } else if (valor.matches("^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[^\\w\\d\\s:])([^\\s]){8,16}\$".toRegex())
        ) {
            correcto = true
        } else {
            correcto = false

            runOnUiThread {
                e.error = "La contraseña debe ser de 8 a 16 carácteres de largo y tener: \n" +
                        "   \t - Una letra mayúscula\n" +
                        "   \t - Una letra minúscula\n" +
                        "   \t - Un número\n" +
                        "   \t - Un símbolo"
            }

        }

        return correcto
    }

    //VALIDADOR DE NOMBRE

    fun validarUsuario(e: EditText): Boolean {
        var correcto: Boolean
        var valor = e.text.toString().trim()

        if (valor.length >= 3 &&
            valor.matches("[A-Z].+".toRegex())
        ) {
            correcto = true
        } else {
            runOnUiThread {
                e.error = "Formato de nombre incorrecto."
            }

            correcto = false
        }

        return correcto
    }

    val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_avatar = uri
            avatar.setImageURI(url_avatar)
        }
    }
}