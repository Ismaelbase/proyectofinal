package com.example.tiendav1

import android.app.DatePickerDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.*

class Admin_editar_evento : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.editar_eventoimagen)
    }
    val nombre: EditText by lazy {
        findViewById(R.id.editar_evento_nombre)
    }
    val precio: EditText by lazy {
        findViewById(R.id.editar_evento_precio)
    }
    val aforo: EditText by lazy {
        findViewById(R.id.editar_evento_aforo)
    }
    val datepicker: ImageView by lazy {
        findViewById(R.id.editar_evento_datepicker)
    }
    val fecha_texto: TextView by lazy {
        findViewById(R.id.editar_evento_fecha_texto)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.editar_evento_boton_volver)
    }
    val boton_aplicar: Button by lazy {
        findViewById(R.id.editar_evento_aplicar_cambios)
    }

    private lateinit var pojo_articulo: Evento
    private var url_foto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_evento)

        supportActionBar!!.hide()

        val id_evento = intent.getStringExtra("ID")

        Utilidades.eventos.child(id_evento!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pojo_articulo = snapshot.getValue(Evento::class.java)!!

                    nombre.setText(pojo_articulo.nombre)
                    precio.setText(pojo_articulo.precio.toString())
                    aforo.setText(pojo_articulo.aforo.toString())
                    fecha_texto.setText(pojo_articulo.fecha)

                    Glide.with(applicationContext)
                        .load(pojo_articulo.url_foto)
                        .into(imagen)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        datepicker.setOnClickListener {
            val calendario = Calendar.getInstance()
            val dia = calendario.get(Calendar.DAY_OF_MONTH)
            val mes = calendario.get(Calendar.MONTH)
            val anyo = calendario.get(Calendar.YEAR)

            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    fecha_texto.text = "$dayOfMonth/${month + 1}/$year"
                },
                anyo,
                mes,
                dia
            )
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }

        imagen.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        boton_aplicar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {

                //Comprobamos si hay cambios en los campos

                if (nombre.text.toString() == pojo_articulo.nombre &&
                    precio.text.toString().toDouble() == pojo_articulo.precio &&
                    aforo.text.toString().toInt() == pojo_articulo.aforo &&
                    fecha_texto.text.toString() == pojo_articulo.fecha &&
                    url_foto == null
                ) {

                    Utilidades.tostadaCorrutina(
                        this@Admin_editar_evento,
                        applicationContext,
                        "No se han realizado cambios"
                    )

                } else if (nombre.text.toString().isEmpty() || precio.text.toString()
                        .isEmpty() || aforo.text.toString().isEmpty()
                ) {
                    Utilidades.tostadaCorrutina(
                        this@Admin_editar_evento,
                        applicationContext,
                        "Rellene todos los campos"
                    )

                } else if (precio.text.toString().toDouble() < 0) {
                    runOnUiThread {
                        precio.error = "El precio no puede ser negativo"
                    }
                } else if (aforo.text.toString().toInt() < 0) {
                    runOnUiThread {
                        aforo.error = "El aforo no puede ser negativo"
                    }

                } else if (Utilidades.existeevento(nombre.text.toString(),fecha_texto.text.toString().trim()) && nombre.text.toString() !=
                    pojo_articulo.nombre
                ) {
                    runOnUiThread {
                        nombre.error = "Ya existe un evento con ese nombre en la fecha indicada"
                    }
                } else if (aforo.text.toString().toInt() < pojo_articulo.apuntados!!.toInt()) {

                    runOnUiThread {
                        aforo.error = "El aforo no puede ser menor que el número de apuntados actual:\n - ${pojo_articulo.apuntados} apuntados"
                    }

                } else {

                    val url_firebase: String?

                    if (url_foto == null) {
                        url_firebase = pojo_articulo.url_foto
                    } else {
                        url_firebase = Utilidades.guardarImagen(
                            referencia_almacenamiento,
                            pojo_articulo.id!!,
                            url_foto!!
                        )
                    }

                    Utilidades.escribirEvento(
                        referencia_bd,
                        pojo_articulo.id!!,
                        nombre.text.toString(),
                        fecha_texto.text.toString(),
                        precio.text.toString().toDouble(),
                        aforo.text.toString().toInt(),
                        url_firebase.toString(),
                        pojo_articulo.apuntados!!.toInt()
                    )

                    Utilidades.tostadaCorrutina(
                        this@Admin_editar_evento, applicationContext,
                        "Cambios aplicados correctamente"
                    )

                    finish()
                }
            }

        }

        boton_volver.setOnClickListener {
            finish()
        }
    }

    private val accesoGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                url_foto = uri
                imagen.setImageURI(url_foto)
            }
        }


}
