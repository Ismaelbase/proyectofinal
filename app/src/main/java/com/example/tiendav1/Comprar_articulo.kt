package com.example.tiendav1

import android.content.Intent
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

class Comprar_articulo : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val nombre:TextView by lazy {
        findViewById(R.id.comprar_nombre)
    }
    val descripcion:TextView by lazy {
        findViewById(R.id.editar_articulo_descripcion)
    }
    val precio:TextView by lazy {
        findViewById(R.id.comprar_precio)
    }
    val stock:TextView by lazy {
        findViewById(R.id.comprar_stock)
    }
    val categoria:TextView by lazy {
        findViewById(R.id.comprar_categoria)
    }
    val reservar:Button by lazy {
        findViewById(R.id.comprar_reserva)
    }
    val imagen:ImageView by lazy {
        findViewById(R.id.comprar_imagen)
    }
    val volver:ImageView by lazy {
        findViewById(R.id.comprar_volver)
    }


    private lateinit var pojo_articulo: Articulo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprar_articulo)


        val id_articulo = intent.getStringExtra("ID").toString()

        referencia_bd.child("SecondCharm")
            .child("Articulos")
            .child(id_articulo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_articulo = snapshot.getValue(Articulo::class.java)!!

                    nombre.setText(pojo_articulo.nombre)
                    descripcion.setText(pojo_articulo.descripcion)
                    precio.setText(pojo_articulo.precio.toString())
                    stock.setText(pojo_articulo.stock.toString())
                    categoria.setText(pojo_articulo.categoria)

                    Glide.with(applicationContext)
                        .load(pojo_articulo.url_foto)
                        .apply(Utilidades.opcionesGlide(applicationContext))
                        .transition(Utilidades.transicion)
                        .into(imagen)

                }
                override fun onCancelled(error: DatabaseError) {
                }
            })


        //Esto comprueba que el articulo no ha sido reservado ya por el usuario, solo se puede reservar una vez, cuando
        // el articulo es recogido o cancelado, se puede volver a reservar
        GlobalScope.launch (Dispatchers.IO){
            if(Utilidades.existeReserva(referencia_bd,id_articulo,Utilidades.obtenerIDUsuario(applicationContext))) {
                runOnUiThread{
                    reservar.isEnabled = false
                    reservar.text = "Reservado"
                }
            }
        }

        reservar.setOnClickListener {
            if(pojo_articulo.stock!! > 0){
                referencia_bd.child("SecondCharm")
                    .child("Articulos")
                    .child(id_articulo)
                    .child("stock")
                    .setValue(pojo_articulo.stock!! - 1)

                reservar.isEnabled = false

                val id_reserva = Utilidades.reservas.push().key!!

                Utilidades.escribirReserva(
                    referencia_bd,
                    id_reserva,
                    Utilidades.obtenerIDUsuario(applicationContext),
                    pojo_articulo.id!!,
                    0
                )


            }
        }





        volver.setOnClickListener {
            startActivity(Intent(applicationContext,Principal_normal::class.java))
            Utilidades.comprar = true
        }

    }

}