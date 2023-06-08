package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.math.RoundingMode
import java.text.DecimalFormat

class Normal_detalles_reserva : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.detalles_imagen)
    }
    val nombre: TextView by lazy {
        findViewById(R.id.detalles_nombre2)
    }
    val precio: TextView by lazy {
        findViewById(R.id.detalles_precio2)
    }
    val fecha: TextView by lazy {
        findViewById(R.id.detalles_fecha2)
    }
    val estado_texto: TextView by lazy {
        findViewById(R.id.detalles_estado_texto)
    }
    val estado_color: ImageView by lazy {
        findViewById(R.id.detalles_estado_color)
    }
    val boton_cancelar_reserva: Button by lazy {
        findViewById(R.id.detalles_cancelar_reserva)
    }
    val boton_recoger_pedido: Button by lazy {
        findViewById(R.id.detalles_recoger_pedido)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.detalles_boton_volver)
    }

    private lateinit var pojo_reserva: Reserva

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_detalles_reserva)

        supportActionBar?.hide()

        val app_id_moneda = getString(com.example.tiendav1.R.string.app_id)
        val sp_moneda = "${app_id_moneda}_moneda"
        var SP_MONEDA = getSharedPreferences(sp_moneda, 0)
        val moneda_elegida = SP_MONEDA.getBoolean("moneda", false)

        val id_reserva = intent.getStringExtra("ID")

        Utilidades.reservas.child(id_reserva!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_reserva = snapshot.getValue(Reserva::class.java)!!

                    Glide.with(applicationContext)
                        .load(pojo_reserva.url_articulo)
                        .apply(Utilidades.opcionesGlide(applicationContext))
                        .transition(Utilidades.transicion)
                        .into(imagen)

                    nombre.text = pojo_reserva.nombre_articulo
                    fecha.text = pojo_reserva.fecha

                    val df = DecimalFormat("#.00")
                    df.roundingMode = RoundingMode.DOWN

                    if(moneda_elegida) {
                        precio.text = df.format(((pojo_reserva.precio!!.toDouble())*Utilidades.monedas.get("USD")!!)).toString()+"$"
                    }else{
                        precio.text = pojo_reserva.precio.toString()+"€"
                    }


                    when (pojo_reserva.estado) {
                        "Pendiente" -> {
                            estado_texto.text = "Pendiente"
                            estado_color.setImageResource(R.drawable.pendiente)

                            boton_cancelar_reserva.isEnabled = true
                            boton_cancelar_reserva.visibility = Button.VISIBLE

                            boton_recoger_pedido.isEnabled = false
                            boton_recoger_pedido.visibility = Button.INVISIBLE

                        }
                        "Completo" -> {
                            estado_texto.text = "Recogido"
                            estado_color.setImageResource(R.drawable.completado)
                            boton_cancelar_reserva.isEnabled = false
                            boton_cancelar_reserva.visibility = Button.INVISIBLE

                            boton_recoger_pedido.isEnabled = false
                            boton_recoger_pedido.visibility = Button.INVISIBLE
                        }
                        "Rechazado" -> {
                            estado_texto.text = "Cancelado"
                            estado_color.setImageResource(R.drawable.rechazar)
                            boton_cancelar_reserva.isEnabled = false
                            boton_cancelar_reserva.visibility = Button.INVISIBLE
                            boton_recoger_pedido.isEnabled = false
                            boton_recoger_pedido.visibility = Button.INVISIBLE
                        }
                        "Listo para recoger" -> {
                            estado_texto.text = "Listo"
                            estado_color.setImageResource(R.drawable.preparando)
                            boton_cancelar_reserva.isEnabled = false
                            boton_cancelar_reserva.visibility = Button.INVISIBLE
                            boton_recoger_pedido.isEnabled = true
                            boton_recoger_pedido.visibility = Button.VISIBLE
                        }
                        "Aceptado" -> {
                            estado_texto.text = "Aceptado"
                            estado_color.setImageResource(R.drawable.preparando)
                            boton_cancelar_reserva.isEnabled = false
                            boton_cancelar_reserva.visibility = Button.INVISIBLE
                            boton_recoger_pedido.isEnabled = false
                            boton_recoger_pedido.visibility = Button.INVISIBLE
                        }
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        boton_cancelar_reserva.setOnClickListener {
            pojo_reserva.estado = "Cancelado"
            pojo_reserva.estado_noti = Estado.MODIFICADO
            Utilidades.reservas.child(pojo_reserva.id!!).setValue(pojo_reserva)

//            Utilidades.reservas.child(id_reserva).child("estado_noti").setValue(Estado.MODIFICADO)
//            Utilidades.reservas.child(id_reserva).child("estado").setValue("Cancelado")
            Toast.makeText(applicationContext, "Reserva cancelada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.normal_detalles_reserva = true
        }

        boton_recoger_pedido.setOnClickListener {
            pojo_reserva.estado = "Completo"
            pojo_reserva.estado_noti = Estado.MODIFICADO
            Utilidades.reservas.child(pojo_reserva.id!!).setValue(pojo_reserva)

//            Utilidades.reservas.child(id_reserva).child("estado_noti").setValue(Estado.MODIFICADO)
//            Utilidades.reservas.child(id_reserva).child("estado").setValue("Completo")

            Toast.makeText(applicationContext, "Pedido recogido", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.normal_detalles_reserva = true
        }

        boton_volver.setOnClickListener {
            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.normal_detalles_reserva = true
        }

    }
}