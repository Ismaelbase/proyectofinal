package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        // -------------- MODO NOCHE --------------
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
                startActivity(Intent(applicationContext, Admin_principal::class.java))
            } else {
                startActivity(Intent(applicationContext, Principal_normal::class.java))
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
    val cuenta_atras: TextView by lazy {
        findViewById(R.id.main_tv_cuentaatras)
    }


    private lateinit var lista_usuarios: MutableList<User>
    var modo_noche: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Esconder la barra del menu
        supportActionBar!!.hide()

        //Prueba acceder a la BD de articulos
        val fecha = LocalDate.now()
        val id_articulo = Utilidades.articulos.push().key!!

//        Utilidades.escribirArticulo(
//            referencia_bd,
//            id_articulo,
//            "Pantalones",
//            8.50,
//            "Estilosos y casi nuevos",
//            "Ropa",
//            "https://cdn1.percentil.com/img/p/9/6/9/15365969-20235681-thickbox.jpg",
//            fecha.toString(),
//            2
//        )

        //Carga el modo en función de la última preferencia elegida

        val app_id = getString(com.example.tiendav1.R.string.app_id)
        val shared_theme = "${app_id}_tema_oscuro"
        var SP = getSharedPreferences(shared_theme, 0)

        modo_noche = SP.getBoolean("modo", false)
        Utilidades.cambiarTema(modo_noche)

        // Obtener lista actual de usuarios en la app
        lista_usuarios = Utilidades.obtenerListaUsers(referencia_bd)

        if(Utilidades.recien_registrado != ""){
            usuario.setText(Utilidades.recien_registrado)
            Utilidades.recien_registrado = ""
        }

        var intentos = 4
        cuenta_atras.visibility = View.INVISIBLE

        login.setOnClickListener {
            if(valido()){
                Utilidades.usuarios.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nombre = usuario.text.toString().trim()
                        val password = contrasena.text.toString().trim()

                        val resultado = snapshot.children.singleOrNull {
                            val pojo_usuario = it.getValue(User::class.java)
                            pojo_usuario!!.usuario == nombre && pojo_usuario!!.contraseña == password && pojo_usuario.alta!!
                        }

                        if (resultado != null){ //El usuario existe y es correcto
                            val pojo_usuario = resultado.getValue(User::class.java)

                            if(pojo_usuario!!.admin!!){ //El usuario es administrador
                                Utilidades.establecerTipoUsuario(applicationContext, true)

                                startActivity(Intent(applicationContext, Admin_principal::class.java))
                            }else{ //El usuario es normal
                                startActivity(Intent(applicationContext, Principal_normal::class.java))
                                Utilidades.establecerTipoUsuario(applicationContext, false)
                            }

                            Utilidades.establecerIDUsuario(applicationContext, pojo_usuario.id!!)

                        }else{//El usuario no existe o no es correcto
                            intentos -= 1

                            if(intentos != 0){
                                Toast.makeText(applicationContext, "Usuario no encontrado, tienes ${intentos} intentos.", Toast.LENGTH_SHORT).show()
                            }else{
                                intentos = 4

                                usuario.isEnabled = false
                                contrasena.isEnabled = false
                                login.isEnabled = false
                                cuenta_atras.visibility = View.VISIBLE

                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        runOnUiThread {
                                            usuario.isEnabled = true
                                            contrasena.isEnabled = true
                                            login.isEnabled = true
                                            cuenta_atras.visibility = View.INVISIBLE

                                        }
                                    }

                                }, 5000)

                                object : CountDownTimer(5000,1000){
                                    override fun onTick(cuenta: Long) {
                                        Toast.makeText(applicationContext, cuenta.toString(), Toast.LENGTH_SHORT)
                                            .show()
                                        cuenta_atras.setText("Espera "+cuenta/1000+" segundos.")
                                    }

                                    override fun onFinish() {
                                        Utilidades.tostadaCorrutina(this@MainActivity,applicationContext,"Puedes intentarlo de nuevo")
                                    }
                                }.start()
                            }




                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


            }

        }








        registro.setOnClickListener {
            startActivity(Intent(applicationContext,Registro::class.java))
        }

    }

    fun valido():Boolean{

        var correcto:Boolean

        if(usuario.text.toString().trim()==""){
            usuario.error="Falta el nombre de usuario"
            correcto=false
        }else if(contrasena.text.toString().trim()==""){
            contrasena.error="Falta la contraseña"
            correcto=false
        }else{
            correcto=true
        }

        return correcto
    }
}