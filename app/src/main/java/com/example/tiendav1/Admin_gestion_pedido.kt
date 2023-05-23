package com.example.tiendav1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class Admin_gestion_pedido : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.gestion_pedido_imagen)
    }
    val nombre_articulo: TextView by lazy {
        findViewById(R.id.gestion_pedido_nombre_articulo)
    }
    val estado_texto: TextView by lazy {
        findViewById(R.id.gestion_pedido_estado_texto)
    }
    val estado_color: ImageView by lazy {
        findViewById(R.id.gestion_pedido_estado_color)
    }
    val nombre_usuario: TextView by lazy {
        findViewById(R.id.gestion_pedido_nombre_usuario)
    }
    val fecha: TextView by lazy {
        findViewById(R.id.gestion_pedido_fecha)
    }
    val stock: TextView by lazy {
        findViewById(R.id.gestion_pedido_stock)
    }
    val rechazar_color: ImageView by lazy {
        findViewById(R.id.gestion_pedido_rechazar1)
    }
    val rechazar_texto: TextView by lazy {
        findViewById(R.id.gestion_pedido_rechazar2)
    }
    val aceptar_color: ImageView by lazy {
        findViewById(R.id.gestion_pedido_aceptar1)
    }
    val aceptar_texto: TextView by lazy {
        findViewById(R.id.gestion_pedido_aceptar2)
    }
    val boton_volver:ImageView by lazy {
        findViewById(R.id.gestion_pedido_boton_volver)
    }

    private lateinit var pojo_usuario : User
    private lateinit var pojo_articulo : Articulo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gestion_pedido)

        val pojo_pedido:Reserva = intent!!.getParcelableExtra("PEDIDO")!!

        val id_usuario = pojo_pedido.id_usuario
        val id_articulo = pojo_pedido.id_articulo

        fecha.text = pojo_pedido.fecha
        estado_texto.text = pojo_pedido.estado

        if(pojo_pedido.estado == "Realizado") {
            estado_color.setImageDrawable(getDrawable(R.drawable.pendiente))
            estado_texto.text = "Pendiente"
        }else if(pojo_pedido.estado == "Aceptado") {
            estado_color.setImageDrawable(getDrawable(R.drawable.aceptar))
            estado_texto.text = "Aceptado"
        }else if(pojo_pedido.estado == "Rechazado") {
            estado_color.setImageDrawable(getDrawable(R.drawable.rechazar))
            estado_texto.text = "Rechazado"
        }else{
            estado_color.setColorFilter(R.color.gris)
            estado_texto.text = "Completado"
        }

        Utilidades.usuarios.child(id_usuario!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_usuario = snapshot.getValue(User::class.java)!!

                    nombre_usuario.text = pojo_usuario.usuario
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        Utilidades.articulos.child(id_articulo!!)
            .addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pojo_articulo = snapshot.getValue(Articulo::class.java)!!

                Glide.with(applicationContext)
                    .load(pojo_articulo.url_foto)
                    .transition(Utilidades.transicion)
                    .into(imagen)

                nombre_articulo.text = pojo_articulo.nombre
                stock.text = pojo_articulo.stock.toString()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


        rechazar_color.setOnClickListener {
            Toast.makeText(applicationContext, "Manten para rechazar", Toast.LENGTH_SHORT).show()
        }
        rechazar_texto.setOnClickListener {
            Toast.makeText(applicationContext, "Manten para rechazar", Toast.LENGTH_SHORT).show()
        }

        aceptar_color.setOnClickListener {
            Toast.makeText(applicationContext, "Manten para aceptar", Toast.LENGTH_SHORT).show()
        }
        aceptar_texto.setOnClickListener {
            Toast.makeText(applicationContext, "Manten para aceptar", Toast.LENGTH_SHORT).show()
        }


        rechazar_color.setOnLongClickListener {

            aceptar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            aceptar_texto.setTextColor(getColor(R.color.black))

            rechazar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            rechazar_texto.setTextColor(getColor(R.color.black))

            aceptar_texto.setOnClickListener(null)
            aceptar_color.setOnClickListener(null)
            rechazar_texto.setOnClickListener(null)
            rechazar_color.setOnClickListener(null)

            estado_texto.text = "Rechazado"
            estado_color.setImageDrawable(getDrawable(R.drawable.rechazar))

            Utilidades.reservas.child(pojo_pedido.id!!).child("estado").setValue("Rechazado")

            Toast.makeText(applicationContext, "Pedido rechazado", Toast.LENGTH_SHORT).show()

            true
        }

        rechazar_texto.setOnLongClickListener {

            aceptar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            aceptar_texto.setTextColor(getColor(R.color.black))

            rechazar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            rechazar_texto.setTextColor(getColor(R.color.black))

            aceptar_texto.setOnClickListener(null)
            aceptar_color.setOnClickListener(null)
            rechazar_texto.setOnClickListener(null)
            rechazar_color.setOnClickListener(null)

            estado_texto.text = "Rechazado"
            estado_color.setImageDrawable(getDrawable(R.drawable.rechazar))

            Utilidades.reservas.child(pojo_pedido.id!!).child("estado").setValue("Rechazado")
            Utilidades.articulos.child(pojo_pedido.id_articulo!!).child("stock").setValue(pojo_articulo.stock!! + 1)

            Toast.makeText(applicationContext, "Pedido rechazado", Toast.LENGTH_SHORT).show()

            true
        }

        aceptar_color.setOnLongClickListener {

            rechazar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            rechazar_texto.setTextColor(getColor(R.color.black))

            aceptar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            aceptar_texto.setTextColor(getColor(R.color.black))

            estado_texto.text = "Aceptado"
            estado_color.setImageDrawable(getDrawable(R.drawable.aceptar))

            aceptar_texto.setOnClickListener(null)
            aceptar_color.setOnClickListener(null)
            rechazar_texto.setOnClickListener(null)
            rechazar_color.setOnClickListener(null)

            Utilidades.reservas.child(pojo_pedido.id!!).child("estado").setValue("Aceptado")

            Toast.makeText(applicationContext, "Pedido aceptado", Toast.LENGTH_SHORT).show()

            true
        }
        aceptar_texto.setOnLongClickListener {

            rechazar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            rechazar_texto.setTextColor(getColor(R.color.black))

            aceptar_color.setImageDrawable(getDrawable(R.drawable.desabilitado))
            aceptar_texto.setTextColor(getColor(R.color.black))

            estado_texto.text = "Aceptado"
            estado_color.setImageDrawable(getDrawable(R.drawable.aceptar))

            aceptar_texto.setOnClickListener(null)
            aceptar_color.setOnClickListener(null)
            rechazar_texto.setOnClickListener(null)
            rechazar_color.setOnClickListener(null)

            Utilidades.reservas.child(pojo_pedido.id!!).child("estado").setValue("Aceptado")

            Toast.makeText(applicationContext, "Pedido aceptado", Toast.LENGTH_SHORT).show()
            true
        }


        boton_volver.setOnClickListener {
            Utilidades.admin_gestion_pedido = true
            startActivity(Intent(applicationContext, Admin_principal::class.java))
        }
    }
}