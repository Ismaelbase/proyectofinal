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
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
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
    val imagen: ImageView by lazy {
        findViewById(R.id.main_iv_logo)
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

        if (Utilidades.recien_registrado != "") {
            usuario.setText(Utilidades.recien_registrado)
            Utilidades.recien_registrado = ""
        }

        var intentos = 4
        cuenta_atras.visibility = View.INVISIBLE


        login.setOnClickListener {
            if (valido()) {
                Utilidades.usuarios.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nombre = usuario.text.toString().trim()
                        val password = contrasena.text.toString().trim()

                        val resultado = snapshot.children.singleOrNull {
                            val pojo_usuario = it.getValue(User::class.java)
                            pojo_usuario!!.usuario == nombre && pojo_usuario!!.contraseña == password
                        }

                        if (resultado != null) { //El usuario existe y es correcto
                            val pojo_usuario = resultado.getValue(User::class.java)

                            if (!pojo_usuario!!.alta!!) {
                                Toast.makeText(
                                    applicationContext,
                                    "Usuario baneado",
                                    Toast.LENGTH_LONG
                                ).show()
                                usuario.text.clear()
                                contrasena.text.clear()
                                return
                            }

                            if (pojo_usuario!!.admin!!) { //El usuario es administrador
                                Utilidades.establecerTipoUsuario(applicationContext, true)

                                startActivity(
                                    Intent(
                                        applicationContext,
                                        Admin_principal::class.java
                                    )
                                )
                            } else { //El usuario es normal
                                startActivity(
                                    Intent(
                                        applicationContext,
                                        Principal_normal::class.java
                                    )
                                )
                                Utilidades.establecerTipoUsuario(applicationContext, false)

                            }

                            Utilidades.establecerIDUsuario(applicationContext, pojo_usuario.id!!)

                        } else {//El usuario no existe o no es correcto
                            intentos -= 1

                            if (intentos != 0) {
                                Toast.makeText(
                                    applicationContext,
                                    "Usuario no encontrado, tienes ${intentos} intentos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
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

                                object : CountDownTimer(5000, 1000) {
                                    override fun onTick(cuenta: Long) {
                                        cuenta_atras.setText("Espera " + cuenta / 1000 + " segundos.")
                                    }

                                    override fun onFinish() {
                                        Utilidades.tostadaCorrutina(
                                            this@MainActivity,
                                            applicationContext,
                                            "Puedes intentarlo de nuevo"
                                        )
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
            startActivity(Intent(applicationContext, Registro::class.java))
        }


        // NOTIFICACIONES EVENTOS
        generador = AtomicInteger(0)
        crearCanalNotificaciones()

        Utilidades.eventos.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_evento = snapshot.getValue(Evento::class.java)
                val id_notificacion = generador.incrementAndGet()

                if (pojo_evento!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext) &&
                    pojo_evento!!.estado_noti == Estado.CREADO
                ) {
                    Utilidades.eventos.child(pojo_evento!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    generarNotificacionEvento(
                        id_notificacion,
                        pojo_evento,
                        "${pojo_evento!!.nombre}",
                        "¡Evento nuevo en SecondCharm!",
                        Normal_inscripcion_evento::class.java
                    )

                    Utilidades.noti_evento_add = true
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_evento = snapshot.getValue(Evento::class.java)
                val id_noti = generador.incrementAndGet()

                if (pojo_evento!!.estado_noti == Estado.MODIFICADO &&
                    pojo_evento!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext)
                ) {

                    Utilidades.eventos.child(pojo_evento!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    if (Utilidades.obtenerTipoUsuario(applicationContext)) {
                        generarNotificacionEvento(
                            id_noti,
                            pojo_evento,
                            "Cambios en ${pojo_evento!!.nombre}",
                            "Un evento ha sido modificado",
                            Admin_editar_evento::class.java
                        )
                    } else {
                        generarNotificacionEvento(
                            id_noti,
                            pojo_evento,
                            "Cambios en ${pojo_evento!!.nombre}",
                            "Un evento ha sido modificado",
                            Normal_inscripcion_evento::class.java
                        )
                    }
                    Utilidades.noti_evento_mod = true

                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                false
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                false
            }

            override fun onCancelled(error: DatabaseError) {
                false
            }

        })

        //NOTIFIACIONES ARTICULOS

        Utilidades.articulos.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_articulo = snapshot.getValue(Articulo::class.java)
                val id_notificacion = generador.incrementAndGet()

                if (pojo_articulo!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext) &&
                    pojo_articulo!!.estado_noti == Estado.CREADO
                ) {

                    Utilidades.articulos.child(pojo_articulo!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    generarNotificacionArticulo(
                        id_notificacion,
                        pojo_articulo,
                        "${pojo_articulo!!.nombre}",
                        "¡Nuevo articulo en venta!",
                        Comprar_articulo::class.java
                    )
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_articulo = snapshot.getValue(Articulo::class.java)
                val id_noti = generador.incrementAndGet()

                if (pojo_articulo!!.estado_noti == Estado.MODIFICADO &&
                    pojo_articulo!!.usuario_noti != Utilidades.obtenerIDUsuario(applicationContext)
                ) {

                    Utilidades.articulos.child(pojo_articulo!!.id.toString())
                        .child("estado_noti").setValue(Estado.NOTIFICADO)

                    if (Utilidades.obtenerTipoUsuario(applicationContext)) {
                        generarNotificacionArticulo(
                            id_noti,
                            pojo_articulo,
                            "Articulo editado",
                            "${pojo_articulo!!.nombre} ha sido modificado",
                            Admin_editar_articulo::class.java
                        )
                    } else {
                        generarNotificacionArticulo(
                            id_noti,
                            pojo_articulo,
                            "Articulo editado",
                            "${pojo_articulo!!.nombre} ha sido modificado",
                            Comprar_articulo::class.java
                        )
                    }

                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                false
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                false
            }

            override fun onCancelled(error: DatabaseError) {
                false
            }

        })

        //NOTIFICACIONES RESERVAS

        Utilidades.reservas.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_reserva = snapshot.getValue(Reserva::class.java)
                val id_notificacion = generador.incrementAndGet()

                if (Utilidades.obtenerTipoUsuario(applicationContext)) {
                    if (pojo_reserva!!.estado_noti == Estado.CREADO) {

                        Utilidades.reservas.child(pojo_reserva!!.id.toString())
                            .child("estado_noti").setValue(Estado.NOTIFICADO)

                        generarNotificacionReserva(
                            id_notificacion,
                            pojo_reserva!!,
                            "${pojo_reserva!!.nombre_usuario} a reservado: ${pojo_reserva!!.nombre_articulo}",
                            "¡Tienes una nueva reserva!",
                            Admin_gestion_pedido::class.java
                        )
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_reserva = snapshot.getValue(Reserva::class.java)
                val id_noti = generador.incrementAndGet()

                //Notificaciones para el admin
                if (Utilidades.obtenerTipoUsuario(applicationContext)) {

                    if (pojo_reserva!!.estado == "Completo") {
                        generarNotificacionReserva(
                            id_noti,
                            pojo_reserva!!,
                            "${pojo_reserva!!.nombre_usuario} a recogido: ${pojo_reserva!!.nombre_articulo}",
                            "¡Pedido completado!",
                            Admin_gestion_pedido::class.java
                        )
                    }else if (pojo_reserva!!.estado == "Cancelado"){
                        generarNotificacionReserva(
                            id_noti,
                            pojo_reserva!!,
                            "${pojo_reserva!!.nombre_usuario} a cancelado: ${pojo_reserva!!.nombre_articulo}",
                            "¡Pedido cancelado!",
                            Admin_gestion_pedido::class.java
                        )
                    }
                    //Notificaciones para el usuario
                } else {
                    if (Utilidades.obtenerIDUsuario(applicationContext) == pojo_reserva!!.id_usuario) {
                        if (pojo_reserva!!.estado == "Aceptado") {
                            generarNotificacionReserva(
                                id_noti,
                                pojo_reserva!!,
                                "Tu pedido de ${pojo_reserva!!.nombre_articulo} ha sido aceptado.",
                                "¡Pedido aceptado!",
                                Normal_detalles_reserva::class.java
                            )
                        }else if (pojo_reserva!!.estado == "Rechazado") {
                            generarNotificacionReserva(
                                id_noti,
                                pojo_reserva!!,
                                "Tu pedido de ${pojo_reserva!!.nombre_articulo} ha sido rechazado.",
                                "¡Pedido rechazado!",
                                Normal_detalles_reserva::class.java
                            )
                        }else if (pojo_reserva!!.estado == "Listo para recoger") {
                            generarNotificacionReserva(
                                id_noti,
                                pojo_reserva!!,
                                "Tu pedido de ${pojo_reserva!!.nombre_articulo} esta listo para recoger.",
                                "¡Pedido listo!",
                                Normal_detalles_reserva::class.java
                            )
                        }
                        Utilidades.reservas.child(pojo_reserva!!.id.toString())
                            .child("estado_noti").setValue(Estado.NOTIFICADO)
                    }

                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                false
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                false
            }

            override fun onCancelled(error: DatabaseError) {
                false
            }

        })

        //NOTIFICACIONES INSCRIPCIONES

        Utilidades.inscripcion.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_inscripcion = snapshot.getValue(Inscripcion::class.java)
                val id_notificacion = generador.incrementAndGet()

                if (Utilidades.obtenerTipoUsuario(applicationContext)) {
                    if (pojo_inscripcion!!.estado_noti == Estado.CREADO && pojo_inscripcion!!.estado == "Pendiente"){
                        Utilidades.inscripcion.child(pojo_inscripcion!!.id.toString())
                            .child("estado_noti").setValue(Estado.NOTIFICADO)

                        generarNotificacionInscripcion(
                            id_notificacion,
                            pojo_inscripcion!!,
                            "${pojo_inscripcion!!.nombre_usuario} se ha inscrito a : ${pojo_inscripcion!!.nombre_evento}",
                            "¡Nueva inscripcion!",
                            Admin_editar_inscripcion::class.java
                        )
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val pojo_inscripcion = snapshot.getValue(Inscripcion::class.java)
                val id_noti = generador.incrementAndGet()

                //Notificaciones para el admin
                if (Utilidades.obtenerTipoUsuario(applicationContext)) {
                    //Notificaciones para el usuario
                } else {
                    if (Utilidades.obtenerIDUsuario(applicationContext) == pojo_inscripcion!!.id_usuario) {
                        if (pojo_inscripcion!!.estado_noti == Estado.CREADO){
                            if (pojo_inscripcion!!.estado == "Aceptado") {
                                generarNotificacionInscripcion(
                                    id_noti,
                                    pojo_inscripcion!!,
                                    "Tu inscripcion a ${pojo_inscripcion!!.nombre_evento} ha sido aceptado.",
                                    "¡Inscripción aceptada!",
                                    Normal_detalle_inscripcion::class.java
                                )
                            }else if (pojo_inscripcion!!.estado == "Rechazado") {
                                generarNotificacionInscripcion(
                                    id_noti,
                                    pojo_inscripcion!!,
                                    "Tu inscripcion a ${pojo_inscripcion!!.nombre_evento} ha sido rechazada.",
                                    "¡Inscripcion rechazada!",
                                    Normal_detalle_inscripcion::class.java
                                )
                            }
                            Utilidades.inscripcion.child(pojo_inscripcion!!.id.toString())
                                .child("estado_noti").setValue(Estado.NOTIFICADO)
                        }
                    }

                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                false
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                false
            }

            override fun onCancelled(error: DatabaseError) {
                false
            }

        })

        //Rotacion de la imagen

        val rotacion = RotateAnimation(
            0f, 180f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )

        rotacion.duration = 1000
        rotacion.interpolator = android.view.animation.LinearInterpolator()
        rotacion.fillAfter = true

        val rotacion2 = RotateAnimation(
            180f, 0f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )

        rotacion2.duration = 1000
        rotacion2.interpolator = android.view.animation.LinearInterpolator()
        rotacion2.fillAfter = true

        val hilo:Thread = object : Thread() {
            override fun run() {
                try {
                    var contador = 0
                    while (true) {
                        sleep(2000)
                        runOnUiThread {
                            if (contador == 0) {
                                imagen.startAnimation(rotacion)
                                contador = 1
                            } else {
                                imagen.startAnimation(rotacion2)
                                contador = 0
                            }
                        }
                        sleep(2000)
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        hilo.start()

    }



    private fun generarNotificacionEvento(
        id_noti: Int,
        pojo: Evento,
        contenido: String,
        titulo: String,
        destino: Class<*>
    ) {

        val idcanal = getString(R.string.id_canal)
        val svg_icono =
            getDrawable(R.drawable.ic_notificacion_eventos)!!.toBitmap(width = 70, height = 70)
        val iconolargo =
            BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_black_24dp)
        val actividad = Intent(applicationContext, destino)

        actividad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        actividad.putExtra("ID", pojo.id.toString())

        val pendingIntent =
            PendingIntent.getActivity(this, 0, actividad, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, idcanal)
            .setLargeIcon(svg_icono)
            .setSmallIcon(R.drawable.ic_notificacion_eventos)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de información")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(id_noti, notification)
        }
    }

    private fun generarNotificacionArticulo(
        id_noti: Int,
        pojo: Articulo,
        contenido: String,
        titulo: String,
        destino: Class<*>
    ) {

        val idcanal = getString(R.string.id_canal)
        val svg_icono = getDrawable(R.drawable.logo_sc_2)!!.toBitmap(width = 70, height = 70)
        val actividad = Intent(applicationContext, destino)

        actividad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        actividad.putExtra("ID", pojo.id.toString())

        val pendingIntent =
            PendingIntent.getActivity(this, 0, actividad, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, idcanal)
            .setLargeIcon(svg_icono)
            .setSmallIcon(R.drawable.logo_sc_2)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de información")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(id_noti, notification)
        }
    }

    private fun generarNotificacionReserva(
        id_noti: Int,
        pojo: Reserva,
        contenido: String,
        titulo: String,
        destino: Class<*>
    ) {

        val idcanal = getString(R.string.id_canal)
        val svg_icono =
            getDrawable(R.drawable.ic_notifications_black_24dp)!!.toBitmap(
                width = 70,
                height = 70
            )
        val iconolargo =
            BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_black_24dp)
        val actividad = Intent(applicationContext, destino)

        actividad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        if(destino == Normal_detalles_reserva::class.java) {
            actividad.putExtra("ID", pojo.id.toString())
        }else{
            actividad.putExtra("PEDIDO", pojo)
        }


        val pendingIntent = PendingIntent.getActivity(this, 0, actividad, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, idcanal)
            .setLargeIcon(svg_icono)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de información")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()


        with(NotificationManagerCompat.from(this)) {
            notify(id_noti, notification)
        }
    }

    private fun generarNotificacionInscripcion(
        id_noti: Int,
        pojo: Inscripcion,
        contenido: String,
        titulo: String,
        destino: Class<*>
    ) {

        val idcanal = getString(R.string.id_canal)
        val svg_icono =
            getDrawable(R.drawable.ic_notifications_black_24dp)!!.toBitmap(
                width = 70,
                height = 70
            )
        val iconolargo =
            BitmapFactory.decodeResource(resources, R.drawable.ic_notifications_black_24dp)
        val actividad = Intent(applicationContext, destino)

        actividad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        actividad.putExtra("ID", pojo.id.toString())


        val pendingIntent = PendingIntent.getActivity(this, 0, actividad, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, idcanal)
            .setLargeIcon(svg_icono)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de información")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(id_noti, notification)
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

    fun valido(): Boolean {

        var correcto: Boolean

        if (usuario.text.toString().trim() == "") {
            usuario.error = "Falta el nombre de usuario"
            correcto = false
        } else if (contrasena.text.toString().trim() == "") {
            contrasena.error = "Falta la contraseña"
            correcto = false
        } else {
            correcto = true
        }

        return correcto
    }
}