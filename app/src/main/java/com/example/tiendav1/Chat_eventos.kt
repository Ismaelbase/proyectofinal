package com.example.tiendav1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class Chat_eventos : AppCompatActivity() {

    val recycler: RecyclerView by lazy {
        findViewById(R.id.chat_evento_recycler)
    }
    val referencia_bd: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference()
    }
    val texto: EditText by lazy {
        findViewById(R.id.chat_evento_texto)
    }
    val boton_volver: ImageView by lazy {
        findViewById(R.id.chat_evento_boton_volver)
    }
    val enviar: Button by lazy {
        findViewById(R.id.chat_evento_boton_enviar)
    }

    private lateinit var lista: MutableList<Mensaje>
    private lateinit var usuario_actual: User
    private lateinit var evento_actual: Evento

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_eventos)
        supportActionBar?.hide()

        lista = mutableListOf()

        val id_evento = intent.getStringExtra("ID")

        Utilidades.eventos.child(id_evento!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    evento_actual = snapshot.getValue(Evento::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        val id_usuario = Utilidades.obtenerIDUsuario(applicationContext)

        Utilidades.usuarios.child(id_usuario)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuario_actual = snapshot.getValue(User::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })



        enviar.setOnClickListener {
            val txt = texto.text.toString().trim()

            if (txt != "") { //Si el mensaje no es vacío
                //Cogemos la fecha de hoy.
                val ahora = Calendar.getInstance()
                val formateador = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
                val fecha_hora = formateador.format(ahora.time)

                val id_mensaje = Utilidades.chat_eventos.push().key
                val mensaje_nuevo = Mensaje(
                    id_mensaje,
                    usuario_actual.id,
                    txt,
                    fecha_hora,
                    "",
                    "",
                    evento_actual.id
                )

                Utilidades.chat_eventos.child(id_mensaje!!).setValue(mensaje_nuevo)

                //Una vez subido el mensaje a firebase, podemos borrar la caja de texto.
                texto.setText("")
            } else {
                Toast.makeText(applicationContext, "Mensaje vacío", Toast.LENGTH_SHORT).show()
            }
        }

        Utilidades.chat_eventos.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                    pojo_mensaje!!.id_receptor = usuario_actual.id

                    if (evento_actual!!.id == pojo_mensaje!!.id_articulo) {
                        lista.add(pojo_mensaje)
                    }

                    if (pojo_mensaje.id_emisor == pojo_mensaje.id_receptor) {
                        pojo_mensaje.imagen_emisor = usuario_actual.url_avatar

                    } else {
                        var semaforo = CountDownLatch(1)

                        Utilidades.usuarios.child(pojo_mensaje.id_emisor!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val pojo_user = snapshot.getValue(User::class.java)
                                    pojo_mensaje.imagen_emisor = pojo_user!!.url_avatar

                                    semaforo.countDown()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        semaforo.await()
                    }

                    runOnUiThread {
                        //El ID de firebase es una marca de tiempo, podemos usarla para ordenar mensajes
                        lista.sortBy { it.id }
                        recycler.scrollToPosition(lista.size - 1)
                        recycler.adapter!!.notifyDataSetChanged()
                    }

                }
            }

            //Me salta excepcion si no dejo todos los miembros nombrados:
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        recycler.adapter = Adaptador_Chat_Articulo(lista)
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.setHasFixedSize(true)

        boton_volver.setOnClickListener {
            finish()
        }

    }
}