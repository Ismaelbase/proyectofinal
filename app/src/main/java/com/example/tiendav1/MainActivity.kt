package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        // -------------- MODO NOCHE --------------
        var modo_noche: Boolean = false
        //Cargamos las Shared Preferences
        val app_id = getString(com.example.tiendav1.R.string.app_id)
        val shared_theme = "${app_id}_tema_oscuro"
        var SP = getSharedPreferences(shared_theme, 0)
        //Carga el modo en función de la última preferencia elegida
        modo_noche = SP.getBoolean("modo", false)
        Utilidades.cambiarTema(modo_noche)

        // -------------- CHECK DE QUE EL USER ESTA LOGEADO --------------

        if (Utilidades.obtenerIDUsuario(applicationContext) != "") { // Si el id no es vacio
            if (Utilidades.obtenerTipoUsuario(applicationContext)) { // Si es admin

            } else {

            }
        }
    }

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }

    val usuario: EditText by lazy {
        findViewById(R.id.main_et_usuario)
    }
    val contrasena: EditText by lazy {
        findViewById(R.id.main_et_contraseña)
    }
    val login: Button by lazy {
        findViewById(R.id.main_button_login)
    }
    val registro: TextView by lazy {
        findViewById(R.id.main_tv_registrarse)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registro.setOnClickListener {
            startActivity(Intent(applicationContext,Registro::class.java))
        }

    }
}