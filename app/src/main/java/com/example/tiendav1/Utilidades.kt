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
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.*
import java.util.concurrent.CountDownLatch

class Utilidades {
    companion object {

        //Volver de actividades:
        var comprar = false
        var admin_editar = false


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

        var recien_registrado = ""

        private val clave_id = "id_usuario"
        private val clave_tipo = "tipo_usuario"

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

        ) = db_ref.child("SecondCharm").child("Users").child(id).setValue(User(
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
            db_ref:DatabaseReference,
            id:String,
            nombre:String,
            precio:Double,
            descripcion:String,
            categoria:String,
            url_foto:String,
            fecha:String,
            cantidad:Int

        ) = db_ref.child("SecondCharm").child("Articulos").child(id).setValue(Articulo(
            id,
            nombre,
            precio,
            descripcion,
            categoria,
            url_foto,
            fecha,
            cantidad,
        ))

        fun escribirReserva(
            db_ref:DatabaseReference,
            id:String,
            id_usuario:String,
            id_articulo:String,
            estado:Int

        ) = db_ref.child("SecondCharm").child("Reservas").child(id).setValue(Reserva(
            id,
            id_usuario,
            id_articulo,
            estado
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

        suspend fun existeUser(db_ref:DatabaseReference,nombre:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            db_ref.child("SecondCharm")
                .child("Users")
                .orderByChild("usuario")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            existe=true
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

        suspend fun existeTema(db_ref:DatabaseReference,nombre:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            db_ref.child("SecondCharm")
                .child("Temas")
                .orderByChild("nombre")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            existe=true
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

        suspend fun existeArticulo(db_ref:DatabaseReference,nombre:String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            db_ref.child("SecondCharm")
                .child("Articulos")
                .orderByChild("nombre")
                .equalTo(nombre)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren()){
                            existe=true
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
        // el articulo es recogido o cancelado, se puede volver a reservar
        suspend fun existeReserva(db_ref:DatabaseReference,id_articulo:String,id_usuario: String):Boolean{
            var existe:Boolean=false

            val semaforo = CountDownLatch(1)

            db_ref.child("SecondCharm")
                .child("Reservas")
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChildren()) {
                            for (reserva in snapshot.children) {
                                if (reserva.child("id_articulo").value.toString() == id_articulo &&
                                    reserva.child("id_usuario").value.toString() == id_usuario &&
                                    reserva.child("estado").value.toString().toInt() == 0 || reserva.child("estado").value.toString().toInt() == 1
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

        fun obtenerListaUsers(db_ref: DatabaseReference):MutableList<User>{
            var lista= mutableListOf<User>()

            //Consulta a la bd
            db_ref.child("SecondCharm")
                .child("Users")
                .addValueEventListener(object : ValueEventListener {

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
//
        fun obtenerListaArticulos(db_ref: DatabaseReference):MutableList<Articulo>{
            var lista = mutableListOf<Articulo>()

            //Consulta a la bd
            db_ref.child("SecondCharm")
                .child("Articulos")
                .addValueEventListener(object : ValueEventListener {

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
