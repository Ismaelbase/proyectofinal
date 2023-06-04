package com.example.tiendav1

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.google.firebase.database.*

class Admin_editar_inscripcion : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val imagen_usuario: ImageView by lazy {
        findViewById(R.id.admin_editar_inscripcion_usuario_imagen)
    }
    val imagen_evento: ImageView by lazy {
        findViewById(R.id.admin_editar_inscripcion_evento_imagen)
    }
    val nombre_usuario:TextView by lazy {
        findViewById(R.id.admin_editar_inscripcion_usuario_nombre)
    }
    val nombre_evento:TextView by lazy {
        findViewById(R.id.admin_editar_inscripcion_evento_nombre)
    }
    val estado_color:ImageView by lazy {
        findViewById(R.id.admin_editar_inscripcion_estado_color)
    }
    val estado_texto:TextView by lazy {
        findViewById(R.id.admin_editar_inscripcion_estado_texto)
    }
    val flecha:ImageView by lazy {
        findViewById(R.id.admin_editar_inscripcion_usuario_flecha)
    }
    val boton_aceptar:Button by lazy {
        findViewById(R.id.admin_editar_inscripcion_aceptar)
    }
    val boton_rechazar:Button by lazy {
        findViewById(R.id.admin_editar_inscripcion_rechazar)
    }
    val boton_volver:ImageView by lazy {
        findViewById(R.id.admin_editar_inscripcion_boton_volver)
    }
    val id_inscripcion:String by lazy {
        intent.getStringExtra("ID").toString()
    }

    private lateinit var pojo_inscripcion:Inscripcion
    private lateinit var pojo_evento:Evento

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_inscripcion)

        supportActionBar?.hide()


        Utilidades.inscripcion.child(id_inscripcion!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_inscripcion = snapshot.getValue(Inscripcion::class.java)!!

                    Glide.with(applicationContext)
                        .load(pojo_inscripcion.url_usuario)
                        .transition(Utilidades.transicion)
                        .into(imagen_usuario)

                    Glide.with(applicationContext)
                        .load(pojo_inscripcion.url_evento)
                        .transition(Utilidades.transicion)
                        .into(imagen_evento)

                    nombre_usuario.text = pojo_inscripcion.nombre_usuario
                    nombre_evento.text = pojo_inscripcion.nombre_evento



                    //todo esto quizas falla sin semaforos?
                    Utilidades.eventos.child(pojo_inscripcion.id_evento!!)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                pojo_evento = snapshot.getValue(Evento::class.java)!!

                                if(pojo_inscripcion.estado == "Aceptado"){
                                    boton_aceptar.isEnabled = false
                                    boton_rechazar.isEnabled = false

                                    estado_color.setImageResource(R.drawable.completado)
                                    estado_texto.text = "Aceptado"

                                }else if(pojo_inscripcion.estado == "Rechazado"){
                                    boton_aceptar.isEnabled = false
                                    boton_rechazar.isEnabled = false
                                    estado_color.setImageResource(R.drawable.rechazar)
                                    flecha.setImageResource(R.drawable.cancelado_sc1)
                                    estado_texto.text = "Rechazado"
                                }else if (pojo_inscripcion.estado == "Pendiente"){
                                    estado_color.setImageResource(R.drawable.pendiente)
                                    estado_texto.text = "Pendiente"
                                }

                                if(pojo_evento.apuntados == pojo_evento.aforo){
                                    estado_color.setImageResource(R.drawable.desabilitado)
                                    estado_texto.text = "Completo"

                                    boton_aceptar.isEnabled = false
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })

                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        boton_aceptar.setOnClickListener {
            pojo_inscripcion.estado = "Aceptado"
            pojo_inscripcion.estado_noti = Estado.CREADO
            Utilidades.inscripcion.child(id_inscripcion).setValue(pojo_inscripcion)

            //Se suma uno a apuntados de ese evento
            pojo_evento.apuntados = pojo_evento.apuntados!!.toInt() + 1
            Utilidades.eventos.child(pojo_inscripcion.id_evento!!).setValue(pojo_evento)

            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.admin_editar_evento = true
        }
        boton_rechazar.setOnClickListener {
            pojo_inscripcion.estado = "Rechazado"
            pojo_inscripcion.estado_noti = Estado.CREADO
            Utilidades.inscripcion.child(id_inscripcion).setValue(pojo_inscripcion)

            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.admin_editar_evento = true
        }

        boton_volver.setOnClickListener {
            finish()
        }

    }
}