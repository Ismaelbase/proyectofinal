package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Normal_inscripcion_evento : AppCompatActivity() {


    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val imagen:ImageView by lazy {
        findViewById(R.id.inscripcion_imagen)
    }
    val nombre: TextView by lazy {
        findViewById(R.id.inscripcion_nombre)
    }
    val precio: TextView by lazy {
        findViewById(R.id.inscripcion_precio)
    }
    val fecha: TextView by lazy {
        findViewById(R.id.inscripcion_fecha)
    }
    val libres: TextView by lazy {
        findViewById(R.id.inscripcion_libres)
    }
    val inscribirse:Button by lazy {
        findViewById(R.id.inscripcion_boton_inscribirse)
    }
    val boton_volver:ImageView by lazy {
        findViewById(R.id.inscripcion_boton_volver)
    }

    private lateinit var pojo_evento: Evento
    private lateinit var pojo_usuario: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_inscripcion_evento)


        val id_evento = intent.getStringExtra("ID").toString()
        val id_usuario = Utilidades.obtenerIDUsuario(applicationContext)

        Utilidades.usuarios.child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_usuario = snapshot.getValue(User::class.java)!!
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        Utilidades.eventos.child(id_evento)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_evento = snapshot.getValue(Evento::class.java)!!

                    val calculo = pojo_evento.aforo!!.toInt() - pojo_evento.apuntados!!.toInt()

                    nombre.setText(pojo_evento.nombre)
                    precio.setText(pojo_evento.precio.toString())
                    libres.setText(calculo.toString())
                    fecha.setText(pojo_evento.fecha)

                    Glide.with(applicationContext)
                        .load(pojo_evento.url_foto)
                        .apply(Utilidades.opcionesGlide(applicationContext))
                        .transition(Utilidades.transicion)
                        .into(imagen)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        //Para comprobar si existe ya la inscripcion
        GlobalScope.launch (Dispatchers.IO){
            if(Utilidades.existeInscripcion(id_evento, id_usuario)){
                runOnUiThread{
                    inscribirse.isEnabled = false
                    inscribirse.text = "Inscrito"
                }
            }
        }

        inscribirse.setOnClickListener {
            val id_inscripcion = Utilidades.reservas.push().key!!

            Utilidades.escribirInscripcion(
                id_inscripcion,
                id_usuario,
                id_evento,
                "Pendiente",
                pojo_usuario.usuario!!,
                pojo_evento.nombre!!,
                pojo_evento.url_foto!!,
                pojo_usuario.url_avatar!!,
                pojo_evento.fecha!!,
                pojo_evento.precio!!,
            )
            inscribirse.isEnabled = false
            inscribirse.text = "Inscrito"
        }


        boton_volver.setOnClickListener {
            finish()
        }
    }
}