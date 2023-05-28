package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class Normal_detalle_inscripcion : AppCompatActivity() {

    val id_inscripcion: String by lazy { intent.getStringExtra("ID")!! }
    val imagen_evento: ImageView by lazy { findViewById(R.id.detalle_inscripcion_imagen) }
    val nombre: TextView by lazy { findViewById(R.id.detalle_inscripcion_nombre_evento) }
    val precio: TextView by lazy { findViewById(R.id.detalle_inscripcion_precio) }
    val fecha: TextView by lazy { findViewById(R.id.detalle_inscripcion_fecha) }
    val estado_texto: TextView by lazy { findViewById(R.id.detalle_inscripcion_estado_texto) }
    val estado_color: ImageView by lazy { findViewById(R.id.detalle_inscripcion_estado_color) }
    val boton_volver: ImageView by lazy { findViewById(R.id.detalle_inscripcion_boton_volver) }

    private lateinit var pojo_inscripcion: Inscripcion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_detalle_inscripcion)

        supportActionBar?.hide()

        Utilidades.inscripcion.child(id_inscripcion!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_inscripcion = snapshot.getValue(Inscripcion::class.java)!!

                    Glide.with(applicationContext)
                        .load(pojo_inscripcion.url_evento)
                        .apply(Utilidades.opcionesGlide(applicationContext))
                        .transition(Utilidades.transicion)
                        .into(imagen_evento)

                    nombre.text = pojo_inscripcion.nombre_evento
                    precio.text = pojo_inscripcion.precio.toString() + "€"
                    fecha.text = pojo_inscripcion.fecha

                    if (pojo_inscripcion.estado == "Pendiente") {
                        estado_texto.text = "Pendiente"
                        estado_color.setImageDrawable(applicationContext.getDrawable(R.drawable.pendiente))
                    } else if (pojo_inscripcion.estado == "Aceptado") {
                        estado_texto.text = "Aceptado"
                        estado_color.setImageDrawable(applicationContext.getDrawable(R.drawable.completado))
                    } else if (pojo_inscripcion.estado == "Rechazado") {
                        estado_texto.text = "Rechazado"
                        estado_color.setImageDrawable(applicationContext.getDrawable(R.drawable.rechazar))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        boton_volver.setOnClickListener {
            finish()
        }

    }
}