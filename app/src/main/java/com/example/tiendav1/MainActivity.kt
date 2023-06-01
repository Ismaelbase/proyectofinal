package com.example.tiendav1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.LocalDate
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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

        // ------------ MONEDA ------------
        val app_id_moneda = getString(com.example.tiendav1.R.string.app_id)
        val sp_moneda = "${app_id_moneda}_moneda"
        var SP_MONEDA = getSharedPreferences(sp_moneda, 0)
        SP_MONEDA.getBoolean("moneda", false)

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
    private lateinit var generador: AtomicInteger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Esconder la barra del menu
        supportActionBar!!.hide()

        //Carga el modo en función de la última preferencia elegida

        val app_id = getString(com.example.tiendav1.R.string.app_id)
        val shared_theme = "${app_id}_tema_oscuro"
        var SP = getSharedPreferences(shared_theme, 0)

        modo_noche = SP.getBoolean("modo", false)
        Utilidades.cambiarTema(modo_noche)

        // Obtener lista actual de usuarios en la app
        lista_usuarios = Utilidades.obtenerListaCompletaUsers()

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
                            pojo_usuario!!.usuario == nombre && pojo_usuario!!.contraseña == password
                        }

                        if (resultado != null){ //El usuario existe y es correcto
                            val pojo_usuario = resultado.getValue(User::class.java)

                            if(!pojo_usuario!!.alta!!){
                                Toast.makeText(applicationContext, "Usuario baneado", Toast.LENGTH_LONG).show()
                                usuario.text.clear()
                                contrasena.text.clear()
                                return
                            }

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



        // NOTIFICACIONES
        generador= AtomicInteger(0)
        crearCanalNotificaciones()

        Utilidades.eventos.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_evento = snapshot.getValue(Evento::class.java)
                val id_notificacion = generador.incrementAndGet()
                val androidID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                if(pojo_evento!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext) &&
                        pojo_evento!!.estado_noti == Estado.CREADO){
                    Utilidades.eventos.child(pojo_evento!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    generarNotificacionEvento(id_notificacion,
                        pojo_evento,
                        "Nuevo evento",
                        "Nuevo evento creado",
                        MainActivity::class.java)

                    Utilidades.noti_evento_add = true
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_evento = snapshot.getValue(Evento::class.java)
                val id_noti = generador.incrementAndGet()

                if(pojo_evento!!.estado_noti == Estado.MODIFICADO &&
                        pojo_evento!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext)){

                    Utilidades.eventos.child(pojo_evento!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    if(Utilidades.obtenerTipoUsuario(applicationContext)){
                        generarNotificacionEvento(id_noti,
                            pojo_evento,
                            "Evento modificado",
                            "Un evento ha sido modificado",
                            Admin_editar_evento::class.java)
                    }else{
                        generarNotificacionEvento(id_noti,
                            pojo_evento,
                            "Evento modificado",
                            "Un evento ha sido modificado",
                            Normal_inscripcion_evento::class.java)
                    }

                    Utilidades.noti_evento_mod = true

                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun generarNotificacionEvento(id_noti:Int, pojo: Evento, contenido:String, titulo:String, destino:Class<*>) {

        val idcanal = getString(R.string.id_canal)
        val iconolargo = BitmapFactory.decodeResource(resources,R.drawable.logo1)
        val actividad = Intent(applicationContext,destino)

        actividad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK )
        actividad.putExtra("ID", pojo.id.toString())

        val pendingIntent = PendingIntent.getActivity(this,0,actividad, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, idcanal)
            .setLargeIcon(iconolargo)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de información")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)){
            notify(id_noti,notification)
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = getString(R.string.nombre_canal)
            val idcanal = getString(R.string.id_canal)
            val descripcion = getString(R.string.description_canal)
            val importancia = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(idcanal, nombre, importancia).apply {
                description = descripcion
            }

            val nm: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
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