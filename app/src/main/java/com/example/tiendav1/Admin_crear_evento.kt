package com.example.tiendav1

import android.app.DatePickerDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.util.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class Admin_crear_evento : AppCompatActivity() {

    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    val referencia_almacenamiento: StorageReference by lazy {
        FirebaseStorage.getInstance().getReference()
    }
    val imagen: ImageView by lazy {
        findViewById(R.id.crear_evento_imagen)
    }
    val nombre: EditText by lazy {
        findViewById(R.id.crear_evento_nombre)
    }
    val precio: EditText by lazy {
        findViewById(R.id.crear_evento_precio)
    }
    val aforo: EditText by lazy {
        findViewById(R.id.crear_evento_aforo)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.crear_evento_boton_volver)
    }
    val boton_crear: Button by lazy {
        findViewById(R.id.crear_evento_boton_crear)
    }
    val datepicker: ImageView by lazy {
        findViewById(R.id.crear_evento_datepicker)
    }
    val fecha_texto: TextView by lazy {
        findViewById(R.id.crear_evento_fecha_texto)
    }

    private var url_avatar: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_crear_evento)

        supportActionBar?.hide()

        imagen.setOnClickListener {
            accesoGaleria.launch("image/*")
        }

        //Cogemos la fecha de hoy en formato europeo
        val fecha_hoy = LocalDate.now()
        val dia = fecha_hoy.dayOfMonth
        val mes = fecha_hoy.monthValue
        val anyo = fecha_hoy.year
        fecha_texto.text = "$dia/$mes/$anyo"

        //Abrimos un datepicker limitado a hoy o fechas futuras para evitar errores
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



        //mostramos la fecha elegida



        boton_crear.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                if (nombre.text.isNotEmpty() && precio.text.isNotEmpty() && aforo.text.isNotEmpty()) {

                    if (Utilidades.existeevento(referencia_bd, nombre.text.toString().trim())) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "Ya existe un evento con ese nombre"
                        )
                    } else if (precio.text.toString().toInt() < 0) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "El precio no puede ser negativo"
                        )
                    } else if (aforo.text.toString().toInt() < 0) {
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "El aforo no puede ser negativo"
                        )
                    } else if (url_avatar == null) {

                        imagen.setImageResource(R.drawable.anadir_imagen_symbol_error)

                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                imagen.setImageResource(R.drawable.anadir_imagen_symbol_sc)
                            }
                        }, 3000)
                        Utilidades.tostadaCorrutina(
                            this@Admin_crear_evento,
                            applicationContext,
                            "Debes añadir una imagen"
                        )
                        //Comprobamos que la fecha elegida es hoy o el futuro

                    }else{
                        val id_evento = Utilidades.eventos.push().key!!
                        GlobalScope.launch(Dispatchers.IO) {


                            val url_firebase = Utilidades.guardarImagen(
                                referencia_almacenamiento,
                                id_evento,
                                url_avatar!!
                            )

                            Utilidades.escribirEvento(
                                referencia_bd,
                                id_evento,
                                nombre.text.toString().trim(),
                                fecha_texto.text.toString().trim(),
                                precio.text.toString().toDouble(),
                                aforo.text.toString().toInt(),
                                url_firebase
                            )

                            Utilidades.tostadaCorrutina(
                                this@Admin_crear_evento,
                                applicationContext,
                                "Evento creado correctamente"
                            )
                            finish()
                        }
                    }
                } else {
                    Utilidades.tostadaCorrutina(
                        this@Admin_crear_evento,
                        applicationContext,
                        "Rellena todos los campos"
                    )
                }
            }
        }










        boton_volver.setOnClickListener {
            finish()
        }


    }

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            url_avatar = uri
            imagen.setImageURI(url_avatar)
        }
    }
}