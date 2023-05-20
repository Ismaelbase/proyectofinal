package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.*

class Cambiar_contrasena : AppCompatActivity() {

    val contraseña_actual: EditText by lazy {
        findViewById(R.id.contrasena_normal_actual_input)
    }
    val contraseña_nueva1: EditText by lazy {
        findViewById(R.id.contrasena_normal_nueva_input)
    }
    val contraseña_nueva2: EditText by lazy {
        findViewById(R.id.contrasena_normal_nueva2_input)
    }
    val boton_aplicar: Button by lazy {
        findViewById(R.id.contrasena_normal_boton_aplicar)
    }
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.contrasena_normal_boton_volver)
    }

    private lateinit var pojo_user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_contrasena)

        supportActionBar!!.hide()

        val id_usuario = Utilidades.obtenerIDUsuario(applicationContext)

        referencia_bd.child("SecondCharm")
            .child("Users")
            .child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_user = snapshot.getValue(User::class.java)!!
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        boton_aplicar.setOnClickListener {
            if (contraseña_actual.text.isEmpty() ||
                contraseña_nueva1.text.isEmpty() ||
                contraseña_nueva2.text.isEmpty()) {
                Toast.makeText(applicationContext, "Rellena todos los campos", Toast.LENGTH_LONG)
                    .show()
            }else if(contraseña_actual.text.toString() != pojo_user.contraseña){
                Toast.makeText(applicationContext, "Contraseña actual incorrecta", Toast.LENGTH_LONG)
                    .show()
            }else if (contraseña_nueva1.text.toString() != contraseña_nueva2.text.toString()){
                Toast.makeText(applicationContext,"Las contraseñas nuevas no coinciden.", Toast.LENGTH_LONG).show()
            }else if (validarContraseña(contraseña_nueva1)){

                val nueva_contrasena = contraseña_nueva1.text.toString()

                Utilidades.escribirUser(
                    referencia_bd,
                    pojo_user.id.toString(),
                    pojo_user.usuario.toString(),
                    nueva_contrasena,
                    pojo_user.correo.toString(),
                    pojo_user.puntos,
                    pojo_user.url_avatar.toString(),
                    pojo_user.fecha.toString(),
                    pojo_user.alta!!,
                    pojo_user.conectado!!,
                    pojo_user.admin!!
                )
                Toast.makeText(applicationContext,"Contraseña actualizada", Toast.LENGTH_LONG).show()

                if (pojo_user.admin!!){
                    val intent = Intent(applicationContext,Admin_principal::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(applicationContext,Principal_normal::class.java)
                    startActivity(intent)
                }

            }
        }

        boton_volver.setOnClickListener {
            if (pojo_user.admin!!){
                val intent = Intent(applicationContext,Admin_principal::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(applicationContext,Principal_normal::class.java)
                startActivity(intent)
            }
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
}