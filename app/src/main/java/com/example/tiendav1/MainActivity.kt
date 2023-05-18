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
//        val fecha = LocalDate.now()
//        Utilidades.escribirArticulo(
//            referencia_bd,
//            "IDEPRUEBA2",
//            "Armario",
//            50.05,
//            "Podrás guardar todo tipo de cosas dentro de el.",
//            "Muebles",
//            "data:image/jpeg;base64,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",
//            fecha.toString(),
//            2,
//            50
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