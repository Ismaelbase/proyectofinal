package com.example.tiendav1

import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch

class Utilidades {
    companion object {

        var monedas = mapOf<String,Double> (
            "EUR" to 1.0,
            "USD" to 1.18
        )

        //Volver de actividades:
        var comprar = false
        var normal_historial = false
        var admin_editar = false
        var admin_anadir = false
        var admin_gestion_pedido = false
        var normal_detalle_evento = false
        var admin_editar_evento = false
        var normal_detalles_reserva = false
        var normal_evento = false

        //Movidas
        var primeraVez = false


        //Volver actividades NOTIFICACIONES
        var noti_evento_mod = false
        var noti_evento_add = false

        //Referencias a bd rapidas
        val usuarios = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Users")

        val articulos = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Articulos")

        val reservas = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Reservas")

        val eventos = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Eventos")
        val inscripcion = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Inscripciones_eventos")

        val chat_productos = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Chat_productos")

        val chat_eventos = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Chat_eventos")

        val chat_foro = FirebaseDatabase.getInstance()
            .getReference()
            .child("SecondCharm")
            .child("Chat_foro")

        var recien_registrado = ""

        private val clave_id = "id_usuario"
        private val clave_tipo = "tipo_usuario"

//        fun formatearFecha(fecha:String):String{
//            val fecha_evento = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//            return fecha_evento.format(DateTimeFormatter.ofPattern("dd/MMMM/yyyy"))
//        }
//
//        fun fechaHoy():String{
//            val fecha_actual = LocalDate.now()
//            return fecha_actual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//        }

        fun fechaFutura(fecha:String):Boolean{
            val fecha_actual = LocalDate.now()
            val fecha_evento = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val dias = ChronoUnit.DAYS.between(fecha_actual,fecha_evento)
            return dias >= 0
        }

        fun cambiarTema(on:Boolean){
            if(on){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        fun obtenerIDUsuario(contexto: Context):String{
            val ID = contexto.getString(R.string.app_id)
            val sp_name = "${ID}_Registro_usuarios"
            val SP = contexto.getSharedPreferences(sp_name, 0)

            return SP.getString(clave_id,"").toString()
        }

        fun establecerIDUsuario(contexto: Context,identificador: String){
            val ID = contexto.getString(R.string.app_id)
            val sp_name = "${ID}_Registro_usuarios"
            val SP = contexto.getSharedPreferences(sp_name, 0)

            with(SP.edit()){
                putString(clave_id,identificador)
                commit()
            }
        }

        fun obtenerTipoUsuario(contexto: Context):Boolean{
            val ID = contexto.getString(R.string.app_id)
            val sp_name = "${ID}_Registro_usuarios"
            val SP = contexto.getSharedPreferences(sp_name, 0)

            return SP.getBoolean(clave_tipo,false)
        }

        fun establecerTipoUsuario(contexto: Context,admin: Boolean){
            val ID = contexto.getString(R.string.app_id)
            val sp_name = "${ID}_Registro_usuarios"
            val SP = contexto.getSharedPreferences(sp_name, 0)

            with(SP.edit()){
                putBoolean(clave_tipo,admin)
                commit()
            }
        }

        //TODO permitir al user hacer fotos para su avatar?
//        fun crearFicheroImagen(contexto:Context): File {
//            val cal: Calendar?= Calendar.getInstance()
//            val timeStamp:String?= SimpleDateFormat("yyyyMMdd_HHmmss").format(cal!!.time)
//            val nombreFichero:String?="JPGE_"+timeStamp+"_"
//            val carpetaDir: File?=contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val ficheroImagen: File?= File.createTempFile(nombreFichero!!,".jpg",carpetaDir)
//
//            return ficheroImagen!!
//        }

        fun escribirUser(
            db_ref:DatabaseReference,
            id:String,
            usuario:String,
            contraseña:String,
            correo:String,
            puntos:Int?,
            url_avatar:String,
            fecha:String,
            alta:Boolean,
            conectado:Boolean,
            admin:Boolean

        ) = usuarios.child(id).setValue(User(
                    id,
                    usuario,
                    contraseña,
                    correo,
                    puntos,
                    url_avatar,
                    fecha,
                    alta,
                    conectado,
                    admin
                ))

        fun escribirArticulo(
            id:String,
            nombre:String,
            precio:Double,
            descripcion:String,
            categoria:String,
            url_foto:String,
            fecha:String,
            stock:Int,
            disponible:Boolean,
            estado_noti:Int,
            usuario_noti:String


        ) = articulos.child(id).setValue(Articulo(
            id,
            nombre,
            precio,
            descripcion,
            categoria,
            url_foto,
            fecha,
            stock,
            disponible,
            estado_noti,
            usuario_noti
        ))

        fun escribirReserva(
            id:String,
            id_usuario:String,
            id_articulo:String,
            estado:String,
            nombre_usuario:String,
            nombre_articulo:String,
            url_articulo:String,
            fecha:String,
            precio:Double,
            estado_noti: Int

        ) = reservas.child(id).setValue(Reserva(
            id,
            id_usuario,
            id_articulo,
            estado,
            nombre_usuario,
            nombre_articulo,
            url_articulo,
            fecha,
            precio,
            estado_noti
        ))

        fun escribirEvento(
            id:String,
            nombre:String,
            fecha:String,
            precio:Double,
            aforo:Int,
            url_foto:String,
            apuntados:Int,
            activo:Boolean,
            estado_noti:Int,
            usuario_noti:String

        ) = eventos.child(id).setValue(Evento(
            id,
            nombre,
            fecha,
            precio,
            aforo,
            url_foto,
            apuntados,
            activo,
            estado_noti,
            usuario_noti
        ))

        fun escribirInscripcion(
            id:String,
            id_usuario:String,
            id_evento:String,
            estado:String,
            nombre_usuario:String,
            nombre_evento:String,
            url_evento:String,
            url_usuario:String,
            fecha:String,
            precio: Double,
            estado_noti: Int

        ) = inscripcion.child(id).setValue(Inscripcion(
            id,
            id_usuario,
            id_evento,
            estado,
            nombre_usuario,
            nombre_evento,
            url_evento,
            url_usuario,
            fecha,
            precio,
            estado_noti

        ))

        fun animacion_carga(contexto: Context): CircularProgressDrawable {
            val animacion=CircularProgressDrawable(contexto)
            animacion.strokeWidth=5f
            animacion.centerRadius=30f
            animacion.start()

            return animacion
        }
        val transicion = DrawableTransitionOptions.withCrossFade(500)
//        val transicion = BitmapTransitionOptions.withCrossFade(500)

        fun opcionesGlide(context: Context): RequestOptions {

            return RequestOptions()
                .placeholder(animacion_carga(context))
                .fallback(R.drawable.fallback)
                .error(R.drawable.error)
        }

        fun opcionesGlideAvatar(context: Context): RequestOptions {

            return RequestOptions()
                .placeholder(animacion_carga(context))
                .fallback(R.drawable.fallback)
                .error(R.drawable.error)
                .apply(circleCropTransform())
        }

        //suspend hace referencia a un metodo que se ejecutará de manera sincronizada con el main en el que se ejecute.
        suspend fun guardarImagen(sto_ref:StorageReference,id:String,imagen:Uri):String{
            lateinit var url_imagen_firebase:Uri

            // .await es un metodo que se puede usar dentro de suspend, para hacer que la funcion se haga de manera sincrona.
            url_imagen_firebase=sto_ref.child("SecondCharm").child("avatares").child(id)
                .putFile(imagen).await().storage.downloadUrl.await()

            return url_imagen_firebase.toString()
        }

        suspend fun existeUser(nombre:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            usuarios.orderByChild("usuario")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            existe=true
                        }
                        semaforo.countDown()
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            semaforo.await()

            return existe
        }

        suspend fun existeArticulo(nombre:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            articulos.orderByChild("nombre")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            existe=true
                        }
                        semaforo.countDown()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            semaforo.await()

            return existe
        }

        suspend fun existeevento(nombre:String,fecha:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)
            var pojo_evento = Evento()

            eventos.orderByChild("nombre")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            pojo_evento=snapshot.children.first().getValue(Evento::class.java)!!
                            if(pojo_evento.fecha==fecha){
                                existe=true
                            }
                        }else{
                        }
                        semaforo.countDown()
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            semaforo.await()
            return existe
        }


        //Esto comprueba que el articulo no ha sido reservado ya por el usuario, solo se puede reservar una vez, cuando
        // el articulo esta 'completo' o 'cancelado', se puede volver a reservar
        suspend fun existeReserva(id_articulo:String,id_usuario: String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            reservas.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChildren()) {
                            for (reserva in snapshot.children) {
                                if (reserva.child("id_articulo").value.toString() == id_articulo &&
                                    reserva.child("id_usuario").value.toString() == id_usuario && (
                                    reserva.child("estado").value.toString() == "Pendiente" ||
                                    reserva.child("estado").value.toString() == "Listo para recoger" ||
                                    reserva.child("estado").value.toString() == "Aceptado")
                                ) {
                                    existe = true
                                }
                            }
                            semaforo.countDown()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            semaforo.await()

            return existe
        }

        suspend fun existeInscripcion(id_evento:String,id_usuario: String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            inscripcion.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (inscripcion in snapshot.children) {
                            if (
                                inscripcion.child("id_evento").value.toString() == id_evento &&
                                inscripcion.child("id_usuario").value.toString() == id_usuario
                            ) {
                                existe = true
                            }
                        }
                        semaforo.countDown()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

            semaforo.await()

            return existe
        }

        //Esto lanza el toast en una hebra a parte, para poder encajarlo en en globalscope(dispatcher.io) sin que falle
        fun tostadaCorrutina(activity: AppCompatActivity, contexto: Context, texto:String){
            activity.runOnUiThread{
                Toast.makeText(
                    contexto,
                    texto,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //TODO adaptar función a la aplicación

        //Obtener lista datos pero no para usar de manera sincrona o sea que algo dependa directamente de el

        fun obtenerListaUsers():MutableList<User>{
            var lista= mutableListOf<User>()

            //Consulta a la bd
            usuarios.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_user = hijo?.getValue(User::class.java)
                            if (pojo_user!!.alta!!) {
                                lista.add(pojo_user!!)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return lista
        }
        fun obtenerListaCompletaUsers():MutableList<User>{
            var lista= mutableListOf<User>()

            //Consulta a la bd
            usuarios.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach{ hijo: DataSnapshot?->
                        val pojo_user = hijo?.getValue(User::class.java)
                            lista.add(pojo_user!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
            return lista
        }

        fun obtenerListaCompletaUsersMenosActual(id_actual:String):MutableList<User>{
            var lista= mutableListOf<User>()

            //Consulta a la bd
            usuarios.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach{ hijo: DataSnapshot?->
                        val pojo_user = hijo?.getValue(User::class.java)
                        if(pojo_user!!.id!=id_actual) {
                            lista.add(pojo_user!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
            return lista
        }
//
        fun obtenerListaArticulos():MutableList<Articulo>{
            var lista = mutableListOf<Articulo>()

            //Consulta a la bd
            articulos.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_articulo = hijo?.getValue(Articulo::class.java)
                            lista.add(pojo_articulo!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return lista
        }

        fun obtenerListaPedidos():MutableList<Reserva>{
            var lista = mutableListOf<Reserva>()

            //Consulta a la bd
            reservas.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_reserva = hijo?.getValue(Reserva::class.java)
                            lista.add(pojo_reserva!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return lista
        }

        fun obtenerListaEventos():MutableList<Evento>{
            var lista = mutableListOf<Evento>()

            //Consulta a la bd
            eventos.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach{ hijo: DataSnapshot?->
                        val pojo_evento = hijo?.getValue(Evento::class.java)
                        lista.add(pojo_evento!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
            return lista
        }

        fun obtenerListaHistorial(id_usuario:String):MutableList<Reserva>{
            var lista = mutableListOf<Reserva>()

            //Consulta a la bd
            reservas.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach{ hijo: DataSnapshot?->
                        val pojo_reserva = hijo?.getValue(Reserva::class.java)

                        if (pojo_reserva!!.id_usuario == id_usuario) {
                            lista.add(pojo_reserva)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
            return lista
        }

        fun obtenerListaInscripciones():MutableList<Inscripcion>{
            var lista= mutableListOf<Inscripcion>()

            inscripcion.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        lista.clear()
                        snapshot.children.forEach{ hijo: DataSnapshot?->
                            val pojo_inscripcion = hijo?.getValue(Inscripcion::class.java)
                                lista.add(pojo_inscripcion!!)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return lista
        }
//
//        //VALIDADOR DE CORREO
//
//        fun validarCorreo(e: EditText): Boolean {
//            var correcto = true
//            var valor = e.text.toString().trim()
//
//            if (valor.isEmpty()) {
//                correcto = false
//                e.error = "El email es obligatorio."
//
//                //Devuelve true si el e-mail introducido tiene el formato correcto
//                //Por lo que ponemos ! ya que queremos que salte cuando NO es correcto.
//            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()) {
//                correcto = false
//                e.error = "El email no tiene un formato correcto."
//            }
//
//            return correcto
//        }
        //VALIDADOR DE NOMBRE

        fun validarUsuario(e: EditText): Boolean {
            var correcto: Boolean
            var valor = e.text.toString().trim()

            if (valor.length >= 3 &&
                valor.matches("[A-Z].+".toRegex())
            ) {
                correcto = true
            } else {
                e.error = "Formato de nombre incorrecto."
                correcto = false
            }

            return correcto
        }


        val imagen_articulos = listOf<Int>(
            R.drawable.articulo_abrigo1,
            R.drawable.articulo_patin1,
            R.drawable.articulo_radio1
        )
    }
}
