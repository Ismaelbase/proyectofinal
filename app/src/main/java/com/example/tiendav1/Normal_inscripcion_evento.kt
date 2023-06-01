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
import java.math.RoundingMode
import java.text.DecimalFormat

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
    val label_libres: TextView by lazy {
        findViewById(R.id.inscripcion_label_libres)
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

        supportActionBar?.hide()

        val app_id_moneda = getString(com.example.tiendav1.R.string.app_id)
        val sp_moneda = "${app_id_moneda}_moneda"
        var SP_MONEDA = getSharedPreferences(sp_moneda, 0)
        val moneda_elegida = SP_MONEDA.getBoolean("moneda", false)


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
                    libres.setText(calculo.toString())
                    fecha.setText(pojo_evento.fecha)

                    val df = DecimalFormat("#.00")
                    df.roundingMode = RoundingMode.DOWN

                    if(moneda_elegida) {
                        precio.text = df.format(((pojo_evento.precio!!.toDouble())*Utilidades.monedas.get("USD")!!)).toString()+"$"
                    }else{
                        precio.text = pojo_evento.precio.toString()+"€"
                    }


                    Glide.with(applicationContext)
                        .load(pojo_evento.url_foto)
                        .apply(Utilidades.opcionesGlide(applicationContext))
                        .transition(Utilidades.transicion)
                        .into(imagen)

                    if(!Utilidades.fechaFutura(pojo_evento.fecha!!)){
                        inscribirse.isEnabled = false
                        inscribirse.text = "Evento pasado"

                        label_libres.text = "Plazas ocupadas"
                        libres.text = pojo_evento.apuntados.toString()

                    }
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

                    if(!Utilidades.fechaFutura(pojo_evento.fecha!!)){
                        inscribirse.isEnabled = false
                        inscribirse.text = "¡Esperamos que \nlo disfrutaras!"

                        label_libres.text = "Plazas ocupadas"
                        libres.text = pojo_evento.apuntados.toString()

                    }

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
            startActivity(Intent(applicationContext,MainActivity::class.java))
            Utilidades.admin_editar_evento = true
        }
    }
}