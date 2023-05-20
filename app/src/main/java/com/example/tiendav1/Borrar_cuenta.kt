package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*

class Borrar_cuenta : AppCompatActivity() {

    val boton_volver: ImageView by lazy {
        findViewById(R.id.borrar_cuenta_boton_volver)
    }
    val boton_volver2: TextView by lazy {
        findViewById(R.id.borrar_cuenta_boton_volver2)
    }
    val boton_borrar_cuenta: Button by lazy {
        findViewById(R.id.borrar_cuenta_boton_borrar)
    }
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }

    private lateinit var pojo_user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrar_cuenta)

        //Borramos la barra de accion
        supportActionBar?.hide()

        val id_usuario = Utilidades.obtenerIDUsuario(applicationContext)

        referencia_bd.child("SecondCharm")
            .child("Users")
            .child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_user = snapshot.getValue(User::class.java)!!
                    setTitle("Eliminar cuenta ${pojo_user.usuario}")
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        boton_volver.setOnClickListener {
            if (pojo_user.admin!!){
                val intent = Intent(applicationContext,Admin_principal::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(applicationContext,Principal_normal::class.java)
                startActivity(intent)
            }
        }
        boton_volver2.setOnClickListener {
            if (pojo_user.admin!!){
                val intent = Intent(applicationContext,Admin_principal::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(applicationContext,Principal_normal::class.java)
                startActivity(intent)
            }
        }
        boton_borrar_cuenta.setOnClickListener {
            Toast.makeText(applicationContext,"Manten presionado el boton para borrar la cuenta",
                Toast.LENGTH_LONG).show()
        }

        boton_borrar_cuenta.setOnLongClickListener {
            Utilidades.escribirUser(
                referencia_bd,
                pojo_user.id.toString(),
                pojo_user.usuario.toString(),
                pojo_user.contraseña.toString(),
                pojo_user.correo.toString(),
                pojo_user.puntos,
                pojo_user.url_avatar.toString(),
                pojo_user.fecha.toString(),
                false,
                pojo_user.conectado!!,
                pojo_user.admin!!
            )

            Utilidades.establecerIDUsuario(applicationContext,"")
            Utilidades.establecerTipoUsuario(applicationContext,false)

            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)

            true
        }

    }
}